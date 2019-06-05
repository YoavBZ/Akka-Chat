package whatsapp;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;

import java.util.ArrayList;
import java.util.HashMap;

import static whatsapp.Requests.*;
import static whatsapp.Responses.*;
import static whatsapp.Utils.print;

public class Server extends AbstractActor {

	/**
	 * Map to points a username to its user ActorRef
	 */
	private HashMap<String, akka.actor.ActorRef> users = new HashMap<>();

	/**
	 * Map to points a group to its group ActorRef
	 */
	private HashMap<String, akka.actor.ActorRef> groups = new HashMap<>();

	/**
	 * Map to points a username to its group ActorRef list
	 */
	private HashMap<String, ArrayList<akka.actor.ActorRef>> usersGroups = new HashMap<>();

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ConnectRequest.class, this::connectHandler)
				.match(DisconnectRequest.class, this::disconnectHandler)
				.match(ActorRefRequest.class, this::actorRefRequestHandler)
				.match(CreateGroupRequest.class, this::createGroupHandler)
				.match(GroupMessage.class, this::groupMsgHandler)
				.build();
	}

	private void groupMsgHandler(GroupMessage message) {
		if (groups.containsKey(message.target)) {
			groups.get(message.target).forward(message, context());
			sender().tell(new GroupMessageResponse(true), self());
		} else {
			sender().tell(new GroupMessageResponse(false), self());
		}
	}

	private void createGroupHandler(CreateGroupRequest request) {
		if (groups.containsKey(request.groupName)) {
			sender().tell(new CreateGroupResponse(false), self());
		} else {
			akka.actor.ActorRef groupRef = getContext()
					.actorOf(Props.create(Group.class, request.groupName, request.admin, users.get(request.admin)),
							request.groupName);
			groups.put(request.groupName, groupRef);
			usersGroups.get(request.admin).add(groupRef);
			sender().tell(new CreateGroupResponse(true), self());
		}
	}

	private void actorRefRequestHandler(ActorRefRequest req) {
		sender().tell(new ActorRefResponse(users.get(req.username)), self());
	}

	/**
	 * Handler to users connect requests
	 *
	 * @param request Connection request containing the username
	 */
	private void connectHandler(ConnectRequest request) {
		print("Got connect request from: %s", request.username);
		if (users.containsKey(request.username)) {
			print("Telling %s: %s", request.username, false);
			sender().tell(new ConnectResponse(false), self());
		} else {
			users.put(request.username, sender());
			usersGroups.put(request.username, new ArrayList<>());
			print("Telling %s: %s", request.username, true);
			sender().tell(new ConnectResponse(true), self());
		}
	}

	/**
	 * Handler to users disconnect
	 * Purges user information and informs the sender upon success, as follows:
	 * 1. Removes user from its groups (group will be closed if the user is its admin).
	 * 2. Removes user from the user-groups map.
	 * 3. Removes user from the connected user list.
	 * 4. Informs sender.
	 */
	private void disconnectHandler(DisconnectRequest request) {
		akka.actor.ActorRef sender = sender();
		for (akka.actor.ActorRef group : usersGroups.get(request.username)) {
			group.tell("removeUser", self());
		}
		usersGroups.remove(request.username);
		users.remove(request.username);
		sender.tell(new DisconnectResponse(true), self());
	}

	/**
	 * Initiates a server actor.
	 */
	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("Server", ConfigFactory.load());
		system.actorOf(Props.create(Server.class), "server");
	}
}