package whatsapp;

import akka.actor.*;
import com.typesafe.config.ConfigFactory;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Scanner;

import static whatsapp.Requests.*;
import static whatsapp.Responses.*;
import static whatsapp.Utils.asker;
import static whatsapp.Utils.print;

public class User extends AbstractActor {

	private String username;
	private ActorRef server;

	/**
	 * Actor behaviour when connected to server
	 */
	private Receive connected = receiveBuilder()
			// Matching front-end command messages
			.matchEquals("/user disconnect", this::disconnectCmdHandler)
			.match(String.class, User::isUserMsgCmd, this::userMsgCmdHandler)
			.match(String.class, User::isGroupMsgCmd, this::groupMsgCmdHandler)
			.match(String.class, User::isCreateGroupCmd, this::createGroupCmdHandler)
			// Matching back-end messages (users/server responses)
			.match(DisconnectResponse.class, this::disconnectResponseHandler)
			.match(Messages.UserTextMessage.class, this::textHandler)
			.match(Messages.UserFileMessage.class, this::fileHandler)
			.match(GroupTextMessage.class, this::groupMsgHandler)
			.build();

	private void groupMsgHandler(GroupMessage message) {
		print("[%s][%s][%s] %s",
				DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()),
				message.target,
				message.sender,
				message.content);
	}

	/**
	 * Actor behaviour when disconnected to server
	 */
	private Receive disconnected = receiveBuilder()
			// Matching front-end command messages
			.match(String.class, User::isConnectCmd, this::connectCmdHandler)
			// Matching backend messages
			.match(ActorIdentity.class, this::identityHandler)
			.match(ConnectResponse.class, this::connectResponseHandler)
			.build();

	@Override
	public Receive createReceive() {
		return disconnected;
	}

	private static boolean isUserMsgCmd(String s) {
		return s.startsWith("/user text") || s.startsWith("/user file");
	}

	private static boolean isCreateGroupCmd(String s) {
		return s.startsWith("/group create");
	}

	private static boolean isGroupMsgCmd(String s) {
		return s.startsWith("/group text") || s.startsWith("/group file");
	}

	private static boolean isConnectCmd(String s) {
		return s.startsWith("/user connect");
	}

	private void createGroupCmdHandler(String s) {
		String groupName = s.substring(14);
		try {
			CreateGroupResponse response =
					(CreateGroupResponse) asker(server, new CreateGroupRequest(groupName, username));
			if (response.value) {
				print("%s created successfully!", groupName);
			} else {
				print("%s already exists!", groupName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void userMsgCmdHandler(String s) {
		try {
			// Removing '/user ' from command string and building message
			Messages.UserMessage message = Messages.buildUserMessage(s.substring(6), username);
			ActorRefResponse response = (ActorRefResponse) asker(server, new ActorRefRequest(message.target));
			if (response != null) {
				response.ref.tell(message, self());
			} else {
				print("%s does not exist!", message.target);
			}
		} catch (FileNotFoundException e) {
			print("%s does not exist!", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void groupMsgCmdHandler(String s) {
		try {
			// Removing '/group ' from command string and building message
			GroupMessage message = buildGroupMessage(s.substring(7), username);
			GroupMessageResponse response = (GroupMessageResponse) asker(server, message);
			if (response == null || !response.value) {
				print("%s does not exist!", message.target);
			}
		} catch (FileNotFoundException e) {
			print("%s does not exist!", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	private void connectCmdHandler(String s) {
		username = s.split(" ")[2];
		try {
			getContext().actorSelection("akka.tcp://Server@127.0.0.1:2552/user/server")
					.tell(new Identify(username), self());
		} catch (ActorNotFound e) {
			print("server is offline!");
		}
	}

	private void disconnectCmdHandler(String s) {
		if (server.isTerminated()) {
			print("server is offline! try again later!");
		} else {
			server.tell(new Requests.DisconnectRequest(username), self());
		}
	}

	private void textHandler(Messages.UserTextMessage t) {
		System.out.println(String.format("[%s][user][%s] %s",
				DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()), t.sender, t.content));
	}

	private void fileHandler(Messages.UserFileMessage f) {
		System.out.println(String.format("[%s][user][%s] %s",
				DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now()), f.sender, f.content.getAbsolutePath()));
	}

	/**
	 * Initiates a user actor.
	 */
	public static void main(String[] args) {
		ActorSystem system =
				ActorSystem.create("User", ConfigFactory.parseResources("user" + args[0] + ".conf"));
		String id = String.valueOf((int) (Math.random() * 100));
		try {
			ActorRef user = system.actorOf(Props.create(User.class), id);
			Scanner scanner = new Scanner(System.in);
			String command;
			while ((command = scanner.nextLine()) != null)
				user.tell(command, ActorRef.noSender());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			system.terminate();
		}
	}
}