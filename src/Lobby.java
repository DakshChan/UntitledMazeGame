public class Lobby {
	public Server.ConnectionHandler[] playerSockets;

	Lobby() {
		playerSockets = new Server.ConnectionHandler[2];
	}
}
