package whatsapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

class Messages {

	static abstract class UserMessage<T> implements Serializable {
		String sender;
		String target;
		T content;
	}

	static UserMessage buildUserMessage(String s, String sender) throws FileNotFoundException {
		return s.startsWith("text ") ?
				new UserTextMessage(s.substring(6), sender) :
				new UserFileMessage(s.substring(6), sender);
	}

	static class UserTextMessage extends UserMessage<String> {

		UserTextMessage(String command, String sender) {
			this.sender = sender;
			target = command.substring(0, command.indexOf(' '));
			content = command.substring(command.indexOf(' ') + 1);
		}
	}

	static class UserFileMessage extends UserMessage<File> {

		UserFileMessage(String command, String sender) throws FileNotFoundException {
			this.sender = sender;
			String[] strings = command.split(" ");
			target = strings[1];
			content = new File(strings[2]);
			if (!content.exists()) {
				throw new FileNotFoundException(strings[2]);
			}
		}
	}
}
