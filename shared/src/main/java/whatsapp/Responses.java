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
		boolean value;

		DisconnectResponse(boolean value) {
			this.value = value;
		}
	}

	static class InviteResponse implements Serializable {
		Boolean approved;
		String sender;
		String groupName;
		String targetUser;

		InviteResponse(Boolean approved, String sender, String groupName, String targetUser) {
			this.approved = approved;
			this.sender = sender;
			this.groupName = groupName;
			this.targetUser = targetUser;
		}
	}

	static class MuteTimedUp implements Serializable {
		String group;

		MuteTimedUp(String group) {
			this.group = group;
		}
	}
}
