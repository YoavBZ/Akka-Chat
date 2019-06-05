package whatsapp;

import java.io.Serializable;

class ActorRefRequest implements Serializable {
	String username;

	ActorRefRequest(String username) {
		this.username = username;
	}
}
