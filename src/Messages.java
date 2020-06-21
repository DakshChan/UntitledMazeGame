public class Messages {
	public static final String SET_USERNAME = "USERNAME\0";
	public static final String START_GAME = "START\0";

	public static boolean compareHeaders(String header, String msg) {
		return (header + "\0").equals(msg);
	}
}
