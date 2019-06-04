package whatsapp;

import java.io.Serializable;

public class Connection {

	public static class ConnectRequest implements Serializable {
		String username;

		public ConnectRequest(String username) {
			this.username = username;
		}
	}

	public static class ConnectResponse implements Serializable {
		boolean value;

		public ConnectResponse(boolean value) {
			this.value = value;
		}
	}

	public static class DisconnectRequest implements Serializable {
		String username;

		public DisconnectRequest(String username) {
			this.username = username;
		}
	}

	public static class DisconnectResponse implements Serializable {
		Boolean value;

		public DisconnectResponse(boolean value) {
			this.value = value;
		}
	}
}
