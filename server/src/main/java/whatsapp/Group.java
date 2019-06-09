package whatsapp;

import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;

import java.util.ArrayList;
import java.util.HashMap;

public class Group {

	final String admin;
	ArrayList<String> coAdmins = new ArrayList<>();
	ArrayList<String> users = new ArrayList<>();
	HashMap<String, Muted> muted = new HashMap<>();
	Router router;

	Group(String admin, ActorRef adminRef) {
		this.admin = admin;
		users.add(admin);
		router = new Router(new BroadcastRoutingLogic()).addRoutee(adminRef);
	}

	static class Muted {
		String user;
		long start;
		long period;
		Cancellable cancellable;


		public Muted(String user, long start, long period, Cancellable cancellable) {
			this.user = user;
			this.start = start;
			this.period = period;
			this.cancellable = cancellable;
		}
	}
}
