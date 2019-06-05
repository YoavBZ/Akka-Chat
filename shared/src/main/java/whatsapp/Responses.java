package whatsapp;

import java.io.Serializable;

class Responses {
	static class ConnectResponse implements Serializable {
		boolean value;

		ConnectResponse(boolean value) {
			this.value = value;
		}
	}

	static class DisconnectResponse implements Serializable {
		Boolean value;

		DisconnectResponse(boolean value) {
			this.value = value;
		}
	}

	static class GroupMessageResponse implements Serializable {
		Boolean value;

		GroupMessageResponse(boolean value) {
			this.value = value;
		}
	}

	static class CreateGroupResponse implements Serializable {
		Boolean value;

		CreateGroupResponse(boolean value) {
			this.value = value;
		}
	}

	static class ActorRefResponse implements Serializable {
		akka.actor.ActorRef ref;

		ActorRefResponse(akka.actor.ActorRef ref) {
			this.ref = ref;
		}
	}
}
