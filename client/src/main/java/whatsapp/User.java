package whatsapp;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.util.Timeout;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.time.Duration;
import java.util.Optional;
import java.util.Scanner;

import static akka.pattern.Patterns.ask;

public class User extends AbstractActor {

	private String username;
	private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	private ActorRef server;

	/**
	 * Actor behaviour when connected to server
	 */
	private Receive connected = receiveBuilder()
			.matchEquals("disconnect", this::disconnectHandler)
			.match(Connection.DisconnectResponse.class, this::disconnectResponseHandler)
			.match(String.class, s -> s.startsWith("text"), this::userTextHandler)
			.match(String.class, s -> s.startsWith("file"), this::userFileHandler)
			.build();

	/**
	 * Actor behaviour when disconnected to server
	 */
	private Receive disconnected = receiveBuilder()
			.match(String.class, s -> s.startsWith("connect"), this::connectHandler)
			.match(ActorIdentity.class, this::identityHandler)
			.match(Connection.ConnectResponse.class, this::connectResponseHandler)
			.build();

	public User(String username) {
		log.info("Constructing user: " + username);
		this.username = username;
	}

	private void userTextHandler(String s) {
		Messages.UserText message = new Messages.UserText(s);
		Timeout t = Timeout.create(Duration.ofSeconds(10));
		Future<Object> f = ask(server, new ActorRefRequest(message.target), t);
		try {
			ActorRef ref = (ActorRef) Await.result(f, t.duration());
			if (ref != null) {
				ref.tell(new Messages.Text(message.text), self());
			} else {
				log.info("{} does not exist!", message.target);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void userFileHandler(String s) {
		Messages.UserFile message = new Messages.UserFile(s);
		Timeout t = Timeout.create(Duration.ofSeconds(10));
		Future<Object> f = ask(server, new ActorRefRequest(message.target), t);
		try {
			ActorRef ref = (ActorRef) Await.result(f, t.duration());
			if (ref != null) {
				ref.tell(new Messages.File(message.file), self());
			} else {
				log.info("{} does not exist!", message.target);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Receive createReceive() {
		return disconnected;
	}

	private void connectResponseHandler(Connection.ConnectResponse response) {
		if (response.value) {
			log.info("{} has connected successfully!", username);
			getContext().become(connected);
		} else {
			log.info("{} is in use!", username);
		}
	}

	private void disconnectResponseHandler(Connection.DisconnectResponse response) {
		if (response.value) {
			log.info("{} has been disconnected successfully!", username);
			getContext().become(disconnected);
		} else {
			log.error("Couldn't disconnect user: {}!", username);
		}
	}

	private void identityHandler(ActorIdentity identity) {
		Optional<ActorRef> actorRef = identity.getActorRef();
		if (actorRef.isPresent()) {
			server = actorRef.get();
			// getContext().watch(server);
			server.tell(new Connection.ConnectRequest(username), self());
		} else {
			log.info("server is offline!");
		}
	}

	private void connectHandler(String s) {
		username = s.split(" ")[1];
		try {
			getContext().actorSelection("akka.tcp://Server@127.0.0.1:2552/user/server")
					.tell(new Identify(username), self());
		} catch (ActorNotFound e) {
			log.info("server is offline!");
		}
	}

	private void disconnectHandler(String s) {
		if (server.isTerminated()) {
			log.info("server is offline! try again later!");
		} else {
			server.tell(new Connection.DisconnectRequest(username), self());
		}
	}

	/**
	 * Initiates a user actor.
	 */
	public static void main(String[] args) {
		ActorSystem system = ActorSystem.create("User", ConfigFactory.load());
		String id = String.valueOf((int) (Math.random() * 100));
		try {
			ActorRef user = system.actorOf(Props.create(User.class, id));
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