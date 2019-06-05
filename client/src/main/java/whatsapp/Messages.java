package whatsapp;

import java.io.FileNotFoundException;
import java.io.Serializable;

class Messages {

	static abstract class UserMessage<T> implements Serializable {
		String sender;
		String target;
		T content;
	}

	static UserMessage buildMessage(String s, String sender) throws FileNotFoundException {
		return s.startsWith("text") ? new Text(s, sender) : new File(s, sender);
	}

	static class Text extends UserMessage<String> {

		Text(String command, String sender) {
			this.sender = sender;
			String[] strings = command.split(" ");
			target = strings[1];
			content = strings[2];
		}
	}

	static class File extends UserMessage<java.io.File> {

		File(String command, String sender) throws FileNotFoundException {
			this.sender = sender;
			String[] strings = command.split(" ");
			target = strings[1];
			content = new java.io.File(strings[2]);
			if (!content.exists()){
				throw new FileNotFoundException(strings[2]);
			}
		}
	}
}
