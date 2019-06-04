package whatsapp;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.ConfigFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class Server extends AbstractActor {

	private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

	/**
	 * Map to points a username to its user ActorRef
	 */
	private HashMap<String, ActorRef> users = new HashMap<>();

	/**
	 * Map to points a group to its group ActorRef
	 */
	private HashMap<String, ActorRef> groups = new HashMap<>();

	/**
	 * Map to points a username to its group ActorRef list
	 */
	private HashMap<String, ArrayList<ActorRef>> usersGroups = new HashMap<>();

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(Connection.ConnectRequest.class, this::connectHandler)
				.match(Connection.DisconnectRequest.class, this::disconnectHandler)
				.match(ActorRefRequest.class, this::actorRefRequestHandler)
				.build();
	}

	private void actorRefRequestHandler(ActorRefRequest req) {
		sender().tell(new GetActorRef(users.get(req.username)), self());
	}

	/**
	 * Handler to users connect requests
	 *
	 * @param request Connection request containing the username
	 */
	private void connectHandler(Connection.ConnectRequest request) {
		log.info("Got connect request from: {}", request.username);
		if (users.containsKey(request.username)) {
			log.info("Telling {}: {}", request.username, false);
			sender().tell(new Connection.ConnectResponse(false), self());
		} else {
			users.put(request.username, sender());
			usersGroups.put(request.username, new ArrayList<>());
			log.info("Telling {}: {}", request.username, true);
			sender().tell(new Connection.ConnectResponse(true), self());
		}
	}

	/**
	 * Handler to users disconnect requests.
	 * Purges user information and informs the sender upon success, as follows:
	 * 1. Removes user from its groups (group will be closed if the user is its admin).
	 * 2. Removes user from the user-groups map.
	 * 3. Removes user from the connected user list.
	 * 4. Informs sender.
	 */
	private void disconnectHandler(Connection.DisconnectRequest request) {
		ActorRef sender = sender();
		for (ActorRef group : usersGroups.get(request.username)) {
			group.tell("removeUser", self());
		}
		usersGroups.remove(request.username);
		users.remove(request.username);
		sender.tell(new Connection.DisconnectResponse(true), self());
	}

	/**
	 * Initiates a server actor.
	 */
	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("Server", ConfigFactory.load());
		system.actorOf(Props.create(Server.class), "server");
	}
}