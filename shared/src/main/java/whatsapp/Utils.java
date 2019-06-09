package whatsapp;

import akka.actor.ActorRef;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.io.FileNotFoundException;
import java.time.Duration;

import static akka.pattern.Patterns.ask;

class Utils {

	static void print(String format, Object... args) {
		System.out.println(String.format(format, args));
	}

	static Object asker(ActorRef server, Object message) {
		Timeout t = Timeout.create(Duration.ofSeconds(20));
		Future<Object> f = ask(server, message, t);
		try {
			return Await.result(f, t.duration());
		} catch (Exception ignored) {
		}
		return null;
	}

	static Object parseCommand(String cmd) throws FileNotFoundException {
		if (cmd.startsWith("/user")) {
			if (cmd.startsWith("/user connect ")) {
				return new Requests.ConnectRequest(cmd.substring(14));
			} else if (cmd.equals("/user disconnect")) {
				return new Requests.DisconnectRequest(null);
			} else if (cmd.startsWith("/user text ")) {
				return new Messages.UserTextMessage(cmd.substring(11), null);
			} else if (cmd.startsWith("/user file ")) {
				return new Messages.UserFileMessage(cmd.substring(11), null);
			}
		} else if (cmd.startsWith("/group")) {
			if (cmd.startsWith("/group text ")) {
				return new Messages.GroupTextMessage(cmd.substring(12), null);
			} else if (cmd.startsWith("/group file ")) {
				return new Messages.GroupFileMessage(cmd.substring(12), null);
			} else if (cmd.startsWith("/group create ")) {
				return new Requests.GroupCreate(cmd.substring(14), null);
			} else if (cmd.startsWith("/group leave ")) {
				return new Requests.GroupLeave(cmd.substring(13));
			} else if (cmd.startsWith("/group user invite ")) {
				return new Requests.GroupInvite(cmd.substring(19));
			} else if (cmd.startsWith("/group user mute ")) {
				return new Requests.GroupMute(cmd.substring(17));
			} else if (cmd.startsWith("/group user unmute ")) {
				return new Requests.GroupUnMute(cmd.substring(19));
			} else if (cmd.startsWith("/group user remove ")) {
				return new Requests.GroupRemove(cmd.substring(19));
			} else if (cmd.startsWith("/group coadmin add ")) {
				return new Requests.CoAdminAdd(cmd.substring(19));
			} else if (cmd.startsWith("/group coadmin remove ")) {
				return new Requests.CoAdminRemove(cmd.substring(22));
			}
		}
		return cmd;
	}
}
