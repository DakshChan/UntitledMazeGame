/**
 * 
 */
public class Messages {
	public static final String CONNECTION_ESTABLISHED = "CONNECTION_ESTABLISHED\0";
	public static final String SET_USERNAME = "USERNAME\0";
	public static final String JOIN_LOBBY = "JOIN_LOBBY\0";
	public static final String LEFT_LOBBY = "LEFT_LOBBY\0";
	public static final String UPDATE_LOBBY = "UPDATE_LOBBY\0";
	public static final String START_GAME = "START\0";
	public static final String MOVED_UP = "MOVED_UP\0";
	public static final String MOVED_DOWN = "MOVED_DOWN\0";
	public static final String MOVED_LEFT = "MOVED_LEFT\0";
	public static final String MOVED_RIGHT = "MOVED_RIGHT\0";
	public static final String PICKED_ITEM = "PICKED_ITEM\0";
	public static final String FINISHED_MAZE = "FINISHED_MAZE\0";
	public static final String END_GAME = "END_GAME\0";

	public static boolean compareHeaders(String header, String msg) {
		return (header + "\0").equals(msg);
	}
}
