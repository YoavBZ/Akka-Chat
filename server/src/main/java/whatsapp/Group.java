package whatsapp;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;

public class Group extends AbstractActor {
	private String name;
	private ActorRef admin;
	private Router router = new Router(new BroadcastRoutingLogic());

	public Group(String name, ActorRef admin) {
		this.name = name;
		this.admin = admin;
		router.addRoutee(admin);
	}

	public Receive createReceive() {
		return receiveBuilder()
//				.match(InviteUserCommand.class, this::inviteUserHandler)
//				.match(MuteUserCommand.class, this::muteUserHandler)
//				.match(UnmuteUserCommand.class, this::unmuteUserHandler)
//				.match(SendBinaryToGroupCommand.class, this::sendBinaryToGroupHandler)
//				.match(SendTextToGroupCommand.class, this::sendTextToGroupHandler)
				.build();
	}
}
