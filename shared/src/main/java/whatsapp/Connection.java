package whatsapp;

import java.io.Serializable;

class Connection {

	static class ConnectRequest implements Serializable {
		String username;

		ConnectRequest(String username) {
			this.username = username;
		}
	}

	static class ConnectResponse implements Serializable {
		boolean value;

		ConnectResponse(boolean value) {
			this.value = value;
		}
	}

	static class DisconnectRequest implements Serializable {
		String username;

		DisconnectRequest(String username) {
			this.username = username;
		}
	}

	static class DisconnectResponse implements Serializable {
		Boolean value;

		DisconnectResponse(boolean value) {
			this.value = value;
		}
	}
}
