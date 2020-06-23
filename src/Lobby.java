import com.sun.source.tree.Tree;

import javax.swing.text.html.parser.Entity;
import java.util.*;

public class Lobby {
	// number of players per each game
	public static final int PLAYERS_PER_GAME = 2;

	public Server.ConnectionHandler[] playerSockets; // sockets of each player in the game
	public ArrayList<String> names; // name of the player in the game
	public boolean[] hasCompleted; // an array that shows which players have finished the maze
	public int[] points; // number of points each player has received
	TreeMap<Long, String> times; // a hashmap of player names and the time it took to finish the maze

	private long startTime; // the time when the game starts

	/**
	 * Constructor that initilizes all the varialbes with the correct size based on PLAYERS_PER_GAME
	 */
	Lobby() {
		playerSockets = new Server.ConnectionHandler[Lobby.PLAYERS_PER_GAME];
		names = new ArrayList<>();
		hasCompleted = new boolean[Lobby.PLAYERS_PER_GAME];
		points = new int[Lobby.PLAYERS_PER_GAME];
		times = new TreeMap<>();
	}

	/**
	 * Sets the startTime. Called when the game starts
	 */
	public void startTimer() {
		startTime = System.nanoTime();
	}

	/**
	 * Ends the timer and calculates the score. Called when a player finishes the maze
	 * @param id the id of the player who finished the maze
	 */
	public void endTimer(int id) {
		long time = System.nanoTime() - startTime;
		times.put(1000000000000L / time + points[id - 1], names.get(id - 1));
	}

	/**
	 * Parses the score hashmap to be sent
	 * @return scores of type String ready to be sent to the client to be parsed back
	 */
	public String parseScores() {
		String scores = "";
		for (Map.Entry<Long, String> entry : times.entrySet()) {
			scores = entry.getValue() + ": " + entry.getKey();
		}

		return scores;
	}

	/**
	 * Sorts a hashmap of type <String, Long>
	 * @param map the map to be sorted
	 * @return result of type HashMap<String, Long> that is the sorted map.
	 */
/*	public static HashMap<String, Long> sortHashMap(HashMap<String, Long> map) {
		List<Map.Entry<String, Long>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());

		HashMap<String, Long> result = new HashMap<>();
		for (Map.Entry<String, Long> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}*/

}
