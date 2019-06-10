package whatsapp;

import java.io.Serializable;

class Requests {

	static abstract class AbstractRequest implements Serializable {
		String sender;
	}

	static class GroupCreate implements Serializable {
		String groupName;
		String admin;

		GroupCreate(String groupName) {
			this.groupName = groupName;
		}
	}

	static class GroupLeave extends AbstractRequest implements Serializable {
		String group;

		GroupLeave(String group) {
			this.group = group;
		}
	}

	static class ActorRefRequest implements Serializable {
		String username;

		ActorRefRequest(String username) {
			this.username = username;
		}
	}

	static class ConnectRequest implements Serializable {
		String username;

		ConnectRequest(String username) {
			this.username = username;
		}
	}

	static class DisconnectRequest implements Serializable {
		String username;

		DisconnectRequest() {
		}
	}

	static class GroupInvite extends AbstractRequest implements Serializable {
		String groupName;
		String targetUser;

		GroupInvite(String cmd) {
			String[] strings = cmd.split(" ");
			this.groupName = strings[0];
			this.targetUser = strings[1];
		}
	}

	static class GroupRemove extends AbstractRequest implements Serializable {
		String groupName;
		String targetUser;

		GroupRemove(String cmd) {
			String[] strings = cmd.split(" ");
			this.groupName = strings[0];
			this.targetUser = strings[1];
		}
	}

	static class AddUserToGroup implements Serializable {
		String group;
		String user;

		AddUserToGroup(String group, String user) {
			this.group = group;
			this.user = user;
		}
	}

	static abstract class CoAdminRequest extends AbstractRequest implements Serializable {
		String group;
		String user;
	}

	static class CoAdminAdd extends CoAdminRequest implements Serializable {
		CoAdminAdd(String cmd) {
			String[] strings = cmd.split(" ");
			this.group = strings[0];
			this.user = strings[1];
		}
	}

	static class CoAdminRemove extends CoAdminRequest implements Serializable {
		CoAdminRemove(String cmd) {
			String[] strings = cmd.split(" ");
			this.group = strings[0];
			this.user = strings[1];
		}
	}

	static class GroupMute extends AbstractRequest implements Serializable {
		String group;
		String target;
		int period;

		GroupMute(String cmd) {
			String[] strings = cmd.split(" ");
			this.group = strings[0];
			this.target = strings[1];
			this.period = Integer.parseInt(strings[2]);
		}
	}

	static class GroupUnMute extends AbstractRequest implements Serializable {
		String group;
		String target;

		GroupUnMute(String cmd) {
			String[] strings = cmd.split(" ");
			this.group = strings[0];
			this.target = strings[1];
		}
	}

}
