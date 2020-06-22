import java.util.ArrayList;

public class Lobby {
	public static final int PLAYERS_PER_GAME = 4;

	public Server.ConnectionHandler[] playerSockets;
	ArrayList<String> names;

	Lobby() {
		playerSockets = new Server.ConnectionHandler[Lobby.PLAYERS_PER_GAME];
		names = new ArrayList<>();
	}
}
