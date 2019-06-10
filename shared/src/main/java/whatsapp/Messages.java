package whatsapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

class Messages {

	// -------------------- User Messages ---------------------

	static abstract class UserMessage<T> implements Serializable {
		String sender;
		String target;
		T content;
	}

	static class UserTextMessage extends UserMessage<String> {

		UserTextMessage(String sender, String target, String content) {
			this.sender = sender;
			this.target = target;
			this.content = content;
		}

		UserTextMessage(String command) {
			target = command.substring(0, command.indexOf(' '));
			content = command.substring(command.indexOf(' ') + 1);
		}
	}

	static class UserFileMessage extends UserMessage<File> {

		UserFileMessage(String command) throws FileNotFoundException {
			target = command.substring(0, command.indexOf(' '));
			String path = command.substring(command.indexOf(' ') + 1);
			content = new File(path);
			if (!content.exists()) {
				throw new FileNotFoundException(path);
			}
		}
	}

	// -------------------- Group Messages ---------------------

	static abstract class GroupMessage<T> extends Requests.AbstractRequest implements Serializable {
		String target;
		T content;
	}

	static class GroupTextMessage extends GroupMessage<String> {

		GroupTextMessage(String command) {
			target = command.substring(0, command.indexOf(' '));
			content = command.substring(command.indexOf(' ') + 1);
		}
	}

	static class GroupFileMessage extends GroupMessage<File> {

		GroupFileMessage(String command) throws FileNotFoundException {
			target = command.substring(0, command.indexOf(' '));
			String path = command.substring(command.indexOf(' ') + 1);
			content = new File(path);
			if (!content.exists()) {
				throw new FileNotFoundException(path);
			}
		}
	}

	static class GroupBroadcast implements Serializable {
		String message;

		GroupBroadcast(String message) {
			this.message = message;
		}
	}
}
