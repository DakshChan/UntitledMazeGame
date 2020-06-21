public class Lobby {
	public static final int PLAYERS_PER_GAME = 1;

	public Server.ConnectionHandler[] playerSockets;

	Lobby() {
		playerSockets = new Server.ConnectionHandler[Lobby.PLAYERS_PER_GAME];
	}
}
