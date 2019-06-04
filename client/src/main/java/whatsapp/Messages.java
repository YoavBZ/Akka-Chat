package whatsapp;

import java.io.File;

class Messages {

	static class Text {
		String message;

		Text(String message) {
			this.message = message;
		}
	}

	static class File {
		java.io.File file;

		File(java.io.File file) {
			this.file = file;
		}
	}

	static class UserText {
		String target;
		String text;

		UserText(String command) {
			String[] strings = command.split(" ");
			this.target = strings[1];
			this.text = strings[2];
		}
	}

	static class UserFile {
		String target;
		java.io.File file;

		UserFile(String command) {
			String[] strings = command.split(" ");
			this.target = strings[1];
			this.file = new java.io.File(strings[2]);
		}
	}
}
