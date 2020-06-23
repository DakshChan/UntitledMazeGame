import javax.swing.text.html.parser.Entity;
import java.util.*;

public class Lobby {
	public static final int PLAYERS_PER_GAME = 3;

	public Server.ConnectionHandler[] playerSockets;
	public ArrayList<String> names;
	public boolean[] hasCompleted;
	public int[] points;
	HashMap<String, Long> times;

	private long startTime;

	Lobby() {
		playerSockets = new Server.ConnectionHandler[Lobby.PLAYERS_PER_GAME];
		names = new ArrayList<>();
		hasCompleted = new boolean[Lobby.PLAYERS_PER_GAME];
		points = new int[Lobby.PLAYERS_PER_GAME];
		times = new HashMap<>();
	}

	public void startTimer() {
		startTime = System.nanoTime();
	}

	public void endTimer(int id) {
		long time = System.nanoTime() - startTime;
		times.put(names.get(id - 1),  1000000000000L / time + points[id - 1]);
	}

	public String parseScores() {
		HashMap sortedTimes = sortHashMap(times);
		String scores = "";
		Iterator iterator = sortedTimes.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry element = (Map.Entry)iterator.next();
			scores += element.getKey() + ": " + element.getValue() + "\t";
		}
		return scores;
	}

	public static HashMap<String, Long> sortHashMap(HashMap<String, Long> map) {
		List<Map.Entry<String, Long>> list = new ArrayList<>(map.entrySet());
		list.sort(Map.Entry.comparingByValue());

		HashMap<String, Long> result = new HashMap<>();
		for (Map.Entry<String, Long> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

}
