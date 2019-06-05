package whatsapp;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.routing.Broadcast;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;

public class Group extends AbstractActor {
	private final String adminName;
	private final String groupName;
	private final ActorRef actorRef;
	private final Router router = new Router(new BroadcastRoutingLogic());

	public Group(String groupName, String adminName, ActorRef adminRef) {
		this.groupName = groupName;
		this.adminName = adminName;
		this.actorRef = adminRef;
		router.addRoutee(adminRef);
	}

	public Receive createReceive() {
		return receiveBuilder()
				.match(Requests.GroupMessage.class, this::messageHandler)
				.build();
	}

	private void messageHandler(Requests.GroupMessage message) {
		router.route(new Broadcast(message), self());
	}
}
