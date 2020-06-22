import java.util.ArrayList;

public class Lobby {
	public static final int PLAYERS_PER_GAME = 1;

	public Server.ConnectionHandler[] playerSockets;
	public ArrayList<String> names;
	public boolean[] hasCompleted;

	Lobby() {
		playerSockets = new Server.ConnectionHandler[Lobby.PLAYERS_PER_GAME];
		names = new ArrayList<>();
		hasCompleted = new boolean[Lobby.PLAYERS_PER_GAME];
	}
}
