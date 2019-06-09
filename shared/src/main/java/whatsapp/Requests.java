package whatsapp;

import java.io.Serializable;

class Requests {

	static class GroupCreate implements Serializable {
		String groupName;
		String admin;

		GroupCreate(String groupName, String admin) {
			this.groupName = groupName;
			this.admin = admin;
		}
	}

	static class GroupLeave implements Serializable {
		String sender;
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

		DisconnectRequest(String username) {
			this.username = username;
		}
	}

	static class GroupInvite implements Serializable {
		String sender;
		String groupName;
		String targetUser;

		GroupInvite(String cmd) {
			String[] strings = cmd.split(" ");
			this.groupName = strings[0];
			this.targetUser = strings[1];
		}
	}

	static class GroupRemove implements Serializable {
		String sender;
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

	static abstract class CoAdminRequest implements Serializable {
		String sender;
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

	static class GroupMute implements Serializable {
		String group;
		String target;
		long period;
		String sender;

		GroupMute(String cmd) {
			String[] strings = cmd.split(" ");
			this.group = strings[0];
			this.target = strings[1];
			this.period = Long.parseLong(strings[2]);
		}
	}

	static class GroupUnMute implements Serializable {
		String group;
		String target;
		String sender;

		GroupUnMute(String cmd) {
			String[] strings = cmd.split(" ");
			this.group = strings[0];
			this.target = strings[1];
		}
	}

}
