package whatsapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

class Requests {

	static class CreateGroupRequest implements Serializable {
		String groupName;
		String admin;

		CreateGroupRequest(String groupName, String admin) {
			this.groupName = groupName;
			this.admin = admin;
		}
	}

	static abstract class GroupMessage<T> implements Serializable {
		String sender;
		String target;
		T content;
	}

	static GroupMessage buildGroupMessage(String s, String sender) throws FileNotFoundException {
		return s.startsWith("text ") ?
				new GroupTextMessage(s.substring(5), sender) :
				new GroupFileMessage(s.substring(5), sender);
	}

	static class GroupTextMessage extends GroupMessage<String> {

		GroupTextMessage(String command, String sender) {
			this.sender = sender;
			target = command.substring(0, command.indexOf(' '));
			content = command.substring(command.indexOf(' ') + 1);
		}
	}

	static class GroupFileMessage extends GroupMessage<File> {

		GroupFileMessage(String command, String sender) throws FileNotFoundException {
			this.sender = sender;
			String[] strings = command.split(" ");
			target = strings[1];
			content = new File(strings[2]);
			if (!content.exists()) {
				throw new FileNotFoundException(strings[2]);
			}
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
}
