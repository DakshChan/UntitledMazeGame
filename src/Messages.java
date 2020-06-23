/**
 * The Messages that the server and client communicate with
 */
public class Messages {
	public static final String CONNECTION_ESTABLISHED = "CONNECTION_ESTABLISHED\0"; // sent when a connection is established
	public static final String SET_USERNAME = "USERNAME\0"; // sent when users enter a username
	public static final String JOIN_LOBBY = "JOIN_LOBBY\0"; // sent when users join a lobby
	public static final String LEFT_LOBBY = "LEFT_LOBBY\0"; // sent when users leave a lobby
	public static final String UPDATE_LOBBY = "UPDATE_LOBBY\0"; // sent when a new user joins a lobby
	public static final String START_GAME = "START\0"; // sent when all players join and the game is ready to start
	public static final String MOVED_UP = "MOVED_UP\0"; // sent when a player moves up
	public static final String MOVED_DOWN = "MOVED_DOWN\0"; // sent when a player moves down
	public static final String MOVED_LEFT = "MOVED_LEFT\0"; // sent when a player moves left
	public static final String MOVED_RIGHT = "MOVED_RIGHT\0"; // sent when a player moves right
	public static final String PICKED_ITEM = "PICKED_ITEM\0"; // sent when an item is picked up
	public static final String FINISHED_MAZE = "FINISHED_MAZE\0"; // sent when a player finishes the maze
	public static final String END_GAME = "END_GAME\0"; // sent when the game is finished

	/**
	 * Compares the message headers to see if they are the same
	 * @param header the header in the message that is received
	 * @param msg the header in Messages to be checked against
	 * @return true if they are the same headers, false otherwise
	 */
	public static boolean compareHeaders(String header, String msg) {
		return (header + "\0").equals(msg);
	}
}
