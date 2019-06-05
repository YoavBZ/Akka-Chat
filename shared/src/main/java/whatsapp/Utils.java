package whatsapp;

import akka.actor.ActorRef;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.time.Duration;

import static akka.pattern.Patterns.ask;

class Utils {

	static void print(String format, Object... args) {
		System.out.println(String.format(format, args));
	}

	static Object asker(ActorRef server, Object message) throws Exception {
		Timeout t = Timeout.create(Duration.ofSeconds(10));
		Future<Object> f = ask(server, message, t);
		return Await.result(f, t.duration());
	}
}
