package whatsapp;

import akka.actor.*;
import akka.routing.Broadcast;
import com.typesafe.config.ConfigFactory;

import java.time.Duration;
import java.util.HashMap;

import static whatsapp.Messages.GroupBroadcast;
import static whatsapp.Messages.GroupMessage;
import static whatsapp.Requests.*;
import static whatsapp.Responses.ConnectResponse;
import static whatsapp.Responses.DisconnectResponse;

public class Server extends AbstractActor {

	/**
	 * Map to points a username to its user ActorRef
	 */
	private HashMap<String, ActorRef> users = new HashMap<>();

	/**
	 * Map to points a group to its group ActorRef
	 */
	private HashMap<String, Group> groups = new HashMap<>();

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ConnectRequest.class, this::connectHandler)
				.match(DisconnectRequest.class, this::disconnectHandler)
				.match(ActorRefRequest.class, this::actorRefRequestHandler)
				.match(GroupCreate.class, this::GroupCreateHandler)
				.match(GroupLeave.class, this::GroupLeaveHandler)
				.match(GroupMessage.class, this::groupMsgHandler)
				.match(GroupInvite.class, this::groupInviteHandler)
				.match(GroupRemove.class, this::groupRemoveHandler)
				.match(GroupMute.class, this::groupMuteHandler)
				.match(GroupUnMute.class, this::groupUnMuteHandler)
				.match(GroupRemove.class, this::groupRemoveHandler)
				.match(AddUserToGroup.class, this::addUserToGroupHandler)
				.match(CoAdminAdd.class, this::addCoAdminHandler)
				.match(CoAdminRemove.class, this::removeCoAdminHandler)
				.build();
	}

	private void groupMuteHandler(GroupMute mute) {
		Group group = groups.get(mute.group);
		if (group == null) {
			sender().tell(mute.group + " does not exist!", self());
		} else if (!users.containsKey(mute.target)) {
			sender().tell(mute.target + " does not exist!", self());
		} else if (!group.admin.equals(mute.sender) && !group.coAdmins.contains(mute.sender)) {
			sender().tell(String.format("You are neither an admin nor a co-admin of %s!", mute.group), self());
		} else if (!group.users.contains(mute.target)) {
			sender().tell(String.format("%s is not in %s!", mute.target, mute.group), self());
		} else if (!group.muted.containsKey(mute.target)) {
			group.coAdmins.remove(mute.target);
			// Setting up scheduler task
			Cancellable cancellable = getContext().system().scheduler().
					scheduleOnce(Duration.ofSeconds(mute.period), () -> {
						users.get(mute.target).tell(new Responses.MuteTimedUp(mute.group), self());
						group.muted.remove(mute.target);
					}, getContext().dispatcher());

			group.muted.put(mute.target, new Group.Muted(mute.target, System.currentTimeMillis(), mute.period, cancellable));
			users.get(mute.target).tell(mute, users.get(mute.sender));
			sender().tell("", self());
		} else {
			sender().tell("", self());
		}
	}

	private void groupUnMuteHandler(GroupUnMute unmute) {
		Group group = groups.get(unmute.group);
		if (group == null) {
			sender().tell(unmute.group + " does not exist!", self());
		} else if (!users.containsKey(unmute.target)) {
			sender().tell(unmute.target + " does not exist!", self());
		} else if (!group.admin.equals(unmute.sender) && !group.coAdmins.contains(unmute.sender)) {
			sender().tell(String.format("You are neither an admin nor a co-admin of %s!", unmute.group), self());
		} else if (!group.users.contains(unmute.target)) {
			sender().tell(String.format("%s is not in %s!", unmute.target, unmute.group), self());
		} else if (!group.muted.containsKey(unmute.target)) {
			sender().tell(String.format("%s is not muted!", unmute.target), self());
		} else {
			group.muted.get(unmute.target).cancellable.cancel();
			group.muted.remove(unmute.target);
			users.get(unmute.target).tell(unmute, users.get(unmute.sender));
			sender().tell("", self());
		}
	}

	private void GroupCreateHandler(GroupCreate request) {
		if (groups.containsKey(request.groupName)) {
			sender().tell(Boolean.FALSE, self());
		} else {
			Group group = new Group(request.admin, users.get(request.admin));
			groups.put(request.groupName, group);
			sender().tell(Boolean.TRUE, self());
		}
	}

	private void GroupLeaveHandler(GroupLeave leave) {
		Group group = groups.get(leave.group);
		if (group == null) {
			sender().tell(leave.group + " does not exist!", self());
		} else if (!group.users.contains(leave.sender)) {
			sender().tell(String.format("%s is not in %s!", leave.sender, leave.group), self());
		} else {
			// User should be removed
			group.users.remove(leave.sender);
			group.muted.remove(leave.sender);
			group.coAdmins.remove(leave.sender);
			group.router.route(new Broadcast(
					new GroupBroadcast(String.format("%s has left %s", leave.sender, leave.group))), self());
			if (group.admin.equals(leave.sender)) {
				group.router.route(new Broadcast(
						new GroupBroadcast(String.format("%s admin has closed %s!", leave.group, leave.group))), self());
				groups.remove(leave.group);
			}
			sender().tell("", self());
		}
	}

	private void addUserToGroupHandler(AddUserToGroup add) {
		groups.get(add.group).users.add(add.user);
	}

	private void addCoAdminHandler(CoAdminAdd add) {
		Group group = groups.get(add.group);
		if (group == null) {
			sender().tell(add.group + " does not exist!", self());
		} else if (!users.containsKey(add.user)) {
			sender().tell(add.user + " does not exist!", self());
		} else if (!group.admin.equals(add.sender) && !group.coAdmins.contains(add.sender)) {
			sender().tell(String.format("You are neither an admin nor a co-admin of %s!", add.group), self());
		} else if (!group.users.contains(add.user)) {
			sender().tell(String.format("%s is not in %s!", add.user, add.group), self());
		} else if (!group.coAdmins.contains(add.user)) {
			group.coAdmins.add(add.user);
			group.muted.remove(add.user);
			users.get(add.user).tell(add, users.get(add.sender));
			sender().tell("", self());
		} else {
			sender().tell("", self());
		}
	}

	private void removeCoAdminHandler(CoAdminRemove remove) {
		Group group = groups.get(remove.group);
		if (group == null) {
			sender().tell(remove.group + " does not exist!", self());
		} else if (!users.containsKey(remove.user)) {
			sender().tell(remove.user + " does not exist!", self());
		} else if (!group.admin.equals(remove.sender) && !group.coAdmins.contains(remove.sender)) {
			sender().tell(String.format("You are neither an admin nor a co-admin of %s!", remove.group), self());
		} else if (!group.users.contains(remove.user)) {
			sender().tell(String.format("%s is not in %s!", remove.user, remove.group), self());
		} else if (group.coAdmins.contains(remove.user)) {
			group.coAdmins.remove(remove.user);
			users.get(remove.user).tell(remove, users.get(remove.sender));
			sender().tell("", self());
		} else {
			sender().tell("", self());
		}
	}

	private void groupRemoveHandler(GroupRemove remove) {
		Group group = groups.get(remove.groupName);
		if (group == null) {
			sender().tell(remove.groupName + " does not exist!", self());
		} else if (!users.containsKey(remove.targetUser)) {
			sender().tell(remove.targetUser + " does not exist!", self());
		} else if (!group.admin.equals(remove.sender) && !group.coAdmins.contains(remove.sender)) {
			sender().tell(String.format("You are neither an admin nor a co-admin of %s!", remove.groupName), self());
		} else if (!group.users.contains(remove.targetUser)) {
			sender().tell(String.format("%s is not in %s!", remove.targetUser, remove.groupName), self());
		} else {
			group.coAdmins.remove(remove.targetUser);
			group.muted.remove(remove.targetUser);
			group.users.remove(remove.targetUser);
			users.get(remove.targetUser).tell(remove, users.get(remove.sender));
			sender().tell("", self());
		}
	}

	private void groupInviteHandler(GroupInvite invite) {
		Group group = groups.get(invite.groupName);
		if (group == null) {
			sender().tell(invite.groupName + " does not exist!", self());
		} else if (!group.admin.equals(invite.sender) && !group.coAdmins.contains(invite.sender)) {
			sender().tell(String.format("You are neither an admin nor a co-admin of %s!", invite.groupName), self());
		} else if (!users.containsKey(invite.targetUser)) {
			sender().tell(invite.targetUser + " does not exist!", self());
		} else if (group.users.contains(invite.targetUser)) {
			sender().tell(String.format("%s is already in %s!", invite.targetUser, invite.groupName), self());
		} else {
			// Sending the invitation to target-user from source-user = sender()
			users.get(invite.targetUser).tell(invite, users.get(invite.sender));
			sender().tell("", self());
		}
	}

	private void groupMsgHandler(GroupMessage message) {
		Group group = groups.get(message.target);
		if (group != null) {
			Group.Muted m = group.muted.get(message.sender);
			if (m != null) {
				sender().tell(String.format("You are muted for %d in %s!",
						System.currentTimeMillis() - (m.start + m.period), message.target), self());
			}
			group.router.route(new Broadcast(message), users.get(message.sender));
			sender().tell(Boolean.TRUE, self());
		} else {
			sender().tell(Boolean.FALSE, self());
		}
	}

	private void actorRefRequestHandler(ActorRefRequest req) {
		sender().tell(users.get(req.username), self());
	}

	/**
	 * Handler to users connect requests
	 *
	 * @param request Connection request containing the username
	 */
	private void connectHandler(ConnectRequest request) {
		if (users.containsKey(request.username)) {
			sender().tell(new ConnectResponse(false), self());
		} else {
			users.put(request.username, sender());
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
		ActorRef sender = sender();
		for (Group group : groups.values()) {
			group.users.remove(request.username);
			group.coAdmins.remove(request.username);
			group.muted.remove(request.username);
		}
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