package whatsapp;

import akka.actor.*;
import com.typesafe.config.ConfigFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Scanner;

import static whatsapp.Messages.*;
import static whatsapp.Requests.*;
import static whatsapp.Responses.*;
import static whatsapp.Utils.asker;
import static whatsapp.Utils.print;

@SuppressWarnings("Duplicates")
public class User extends AbstractActor {

	private String username;
	private ActorRef server;

	/**
	 * Actor behaviour when connected to server
	 */
	private Receive connected = receiveBuilder()
			// Matching front-end command messages
			.match(DisconnectRequest.class, this::disconnectCmdHandler)
			.match(UserMessage.class, message -> message.sender == null, this::userMsgCmdHandler)
			.match(GroupMessage.class, message -> message.sender == null, this::groupMsgCmdHandler)
			.match(GroupInvite.class, invite -> invite.sender == null, this::groupInviteCmdHandler)
			.match(GroupRemove.class, remove -> remove.sender == null, this::groupRemoveCmdHandler)
			.match(GroupCreate.class, this::createGroupCmdHandler)
			.match(CoAdminRequest.class, add -> add.sender == null, this::coAdminCmdHandler)
			.match(GroupLeave.class, this::groupLeaveCmdHandler)
			.match(GroupMute.class, mute -> mute.sender == null, this::groupMuteCmdHandler)
			.match(GroupUnMute.class, unmute -> unmute.sender == null, this::groupUnMuteCmdHandler)
			// Matching back-end messages (users/server responses)
			.match(DisconnectResponse.class, this::disconnectResponseHandler)
			.match(UserTextMessage.class, this::textHandler)
			.match(UserFileMessage.class, this::fileHandler)
			.match(GroupTextMessage.class, this::groupTextHandler)
			.match(GroupFileMessage.class, this::groupFileHandler)
			.match(GroupInvite.class, this::groupInviteHandler)
			.match(InviteResponse.class, this::inviteResponseHandler)
			.match(GroupRemove.class, this::removeResponseHandler)
			.match(GroupMute.class, this::groupMuteHandler)
			.match(GroupUnMute.class, this::groupUnMuteHandler)
			.match(CoAdminAdd.class, this::coAdminAddHandler)
			.match(CoAdminRemove.class, this::coAdminRemoveHandler)
			.match(GroupBroadcast.class, this::groupBroadcastHandler)
			.match(MuteTimedUp.class, this::muteTimedUPHandler)
			.build();

	/**
	 * Actor behaviour when disconnected to server
	 */
	private Receive disconnected = receiveBuilder()
			// Matching front-end command messages
			.match(ConnectRequest.class, this::connectCmdHandler)
			// Matching backend messages
			.match(ActorIdentity.class, this::identityHandler)
			.match(ConnectResponse.class, this::connectResponseHandler)
			.build();

	@Override
	public Receive createReceive() {
		// User starts disconnected
		return disconnected;
	}

	private void createGroupCmdHandler(GroupCreate request) {
		request.admin = username;
		boolean succeeded = (boolean) asker(server, request);
		if (succeeded) {
			print("%s created successfully!", request.groupName);
		} else {
			print("%s already exists!", request.groupName);
		}
	}

	private void groupLeaveCmdHandler(GroupLeave leave) {
		leave.sender = username;
		String response = (String) asker(server, leave);
		if (response != null && !response.equals("")) {
			print(response);
		}
	}

	private void connectCmdHandler(ConnectRequest request) {
		username = request.username;
		try {
			getContext().actorSelection("akka.tcp://Server@127.0.0.1:2552/user/server")
					.tell(new Identify(username), self());
		} catch (ActorNotFound e) {
			print("server is offline!");
		}
	}

	private void disconnectCmdHandler(DisconnectRequest r) {
		if (server.isTerminated()) {
			print("server is offline! try again later!");
		} else {
			server.tell(new Requests.DisconnectRequest(username), self());
		}
	}

	private void userMsgCmdHandler(UserMessage message) {
		ActorRef target = (ActorRef) asker(server, new ActorRefRequest(message.target));
		if (target != null) {
			message.sender = username;
			target.tell(message, self());
		} else {
			print("%s does not exist!", message.target);
		}
	}

	private void groupMsgCmdHandler(GroupMessage message) {
		message.sender = username;
		boolean succeeded = (boolean) asker(server, message);
		if (!succeeded) {
			print("%s does not exist!", message.target);
		}
	}

	private void groupInviteCmdHandler(GroupInvite invite) {
		invite.sender = username;
		String response = (String) asker(server, invite);
		if (response != null && !response.equals("")) {
			print(response);
		}
	}

	private void groupRemoveCmdHandler(GroupRemove remove) {
		remove.sender = username;
		String response = (String) asker(server, remove);
		if (response != null && !response.equals("")) {
			print(response);
		}
	}

	private void groupMuteCmdHandler(GroupMute mute) {
		mute.sender = username;
		String response = (String) asker(server, mute);
		if (response != null && !response.equals("")) {
			print(response);
		}
	}

	private void groupUnMuteCmdHandler(GroupUnMute unmute) {
		unmute.sender = username;
		String response = (String) asker(server, unmute);
		if (response != null && !response.equals("")) {
			print(response);
		}
	}

	private void coAdminCmdHandler(CoAdminRequest request) {
		request.sender = username;
		String response = (String) asker(server, request);
		if (response != null && !response.equals("")) {
			print(response);
		}
	}

	private void groupTextHandler(GroupTextMessage message) {
		print("[%s][%s][%s] %s",
				DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()),
				message.target,
				message.sender,
				message.content);
	}

	private void groupFileHandler(GroupFileMessage message) {
		File inputFile = new File(message.content.getName());
		try {
			OutputStream outputStream = new FileOutputStream(inputFile);
			InputStream inputStream = new FileInputStream(message.content);
			int read;
			int offset = 0;
			byte[] buffer = new byte[1024];
			while ((read = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, offset, read);
				offset += read;
			}
		} catch (Exception ignored) {
		}
		print("[%s][%s][%s] %s",
				DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()),
				message.target,
				message.sender,
				inputFile.getAbsolutePath());
	}

	private void groupInviteHandler(GroupInvite invite) {
		print(String.format("You have been invited to %s, Accept?", invite.groupName));
		ActorRef sender = sender();
		Receive waitForInviteResponse = receiveBuilder()
				.matchEquals("Yes", s -> {
					sender.tell(new Responses.InviteResponse(true, invite.sender, invite.groupName,
							invite.targetUser), self());
					getContext().become(connected);
				})
				.matchEquals("No", s -> {
					sender.tell(new Responses.InviteResponse(false, invite.sender, invite.groupName,
							invite.targetUser), self());
					getContext().become(connected);
				})
				.build();
		getContext().become(waitForInviteResponse);
	}

	private void inviteResponseHandler(Responses.InviteResponse response) {
		if (response.approved) {
			server.tell(new AddUserToGroup(response.groupName, response.targetUser), self());
			sender().tell(new UserTextMessage(username, response.targetUser,
					String.format("Welcome to %s!", response.groupName)), self());
		}
	}

	private void groupMuteHandler(GroupMute mute) {
		print("You have been muted for %d in %s by %s!", mute.period, mute.group, mute.sender);
	}

	private void groupUnMuteHandler(GroupUnMute unmute) {
		print("You have been unmuted in %s by %s!", unmute.group, unmute.sender);
	}

	private void muteTimedUPHandler(MuteTimedUp m) {
		print("You have been unmuted from %s! Muting time is up!", m.group);
	}

	private void removeResponseHandler(GroupRemove remove) {
		print("[%s][%s][%s]: You have been removed from %s by %s!",
				DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()), remove.groupName, remove.sender,
				remove.groupName, remove.sender);
	}

	private void coAdminAddHandler(CoAdminAdd add) {
		print("You have been promoted to co-admin in %s!", add.group);
	}

	private void coAdminRemoveHandler(CoAdminRemove remove) {
		print("You have been demoted to user in %s!", remove.group);
	}

	private void connectResponseHandler(ConnectResponse response) {
		if (response.value) {
			print("%s has connected successfully!", username);
			getContext().become(connected);
		} else {
			print("%s is in use!", username);
		}
	}

	private void disconnectResponseHandler(DisconnectResponse response) {
		if (response.value) {
			print("%s has been disconnected successfully!", username);
			getContext().become(disconnected);
		} else {
			print("Couldn't disconnect user: %s!", username);
		}
	}

	private void identityHandler(ActorIdentity identity) {
		Optional<ActorRef> actorRef = identity.getActorRef();
		if (actorRef.isPresent()) {
			server = actorRef.get();
			// getContext().watch(server);
			server.tell(new Requests.ConnectRequest(username), self());
		} else {
			print("server is offline!");
		}
	}

	private void textHandler(UserTextMessage t) {
		System.out.println(String.format("[%s][user][%s] %s",
				DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()), t.sender, t.content));
	}

	private void fileHandler(UserFileMessage f) {
		File inputFile = new File(f.content.getName());
		try {
			OutputStream outputStream = new FileOutputStream(inputFile);
			InputStream inputStream = new FileInputStream(f.content);
			int read;
			int offset = 0;
			byte[] buffer = new byte[1024];
			while ((read = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, offset, read);
				offset += read;
			}
		} catch (Exception ignored) {
		}
		System.out.println(String.format("[%s][user][%s] %s",
				DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()), f.sender, inputFile.getAbsolutePath()));
	}

	private void groupBroadcastHandler(GroupBroadcast broadcast) {
		print(broadcast.message);
	}

	/**
	 * Initiates a user actor.
	 */
	public static void main(String[] args) {
		ActorSystem system =
				ActorSystem.create("User", ConfigFactory.parseResources("user" + args[0] + ".conf"));
		String id = String.valueOf((int) (Math.random() * 100));
		ActorRef user = system.actorOf(Props.create(User.class), id);
		Scanner scanner = new Scanner(System.in);
		String command;
		while ((command = scanner.nextLine()) != null) {
			if ("exit".equals(command)) {
				break;
			}
			try {
				user.tell(Utils.parseCommand(command), ActorRef.noSender());
			} catch (FileNotFoundException e) {
				print("%s does not exist!", e.getMessage());
			}
		}
		system.terminate();
	}
}