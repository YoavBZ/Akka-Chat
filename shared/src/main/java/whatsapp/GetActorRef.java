package whatsapp;

import akka.actor.ActorRef;

import java.io.Serializable;

class GetActorRef implements Serializable {
	ActorRef ref;

	GetActorRef(ActorRef ref) {
		this.ref = ref;
	}
}
