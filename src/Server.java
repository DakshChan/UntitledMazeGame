//imports for network communication
import java.io.*;
import java.net.*;
import java.util.ArrayList;

class Server {
	final String LOCAL_HOST = "127.0.0.1";
	final int PORT = 5000;

	ServerSocket serverSocket;//server socket for connection
	int clientCounter = 0;
	ArrayList<Lobby> lobbies;

	public static void main(String[] args) {
		Server server = new Server();
		server.go();
	}

	public void go() {
		lobbies = new ArrayList<>();
		//create a socket with the local IP address (try-catch required) and wait for connection request
		System.out.println("Waiting for a connection request from a client ...");
		try {
			serverSocket = new ServerSocket(PORT);          //create and bind a socket
			while(true) {
				Socket socket = serverSocket.accept();      //wait for connection request
				clientCounter = clientCounter +1;
				System.out.println("Client "+clientCounter+" connected");
				Thread connectionThread = new Thread(new ConnectionHandler(socket));
				connectionThread.start();                   //start a new thread to handle the connection
			}
		} catch(Exception e) {
			System.out.println("Error accepting connection");
			e.printStackTrace();
		}
	}

	//------------------------------------------------------------------------------
	class ConnectionHandler extends Thread {
		Socket socket;            //socket to handle
		PrintWriter output;       //writer for the output stream
		BufferedReader input;     //reader for the input stream

		public ConnectionHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				InputStreamReader stream = new InputStreamReader(socket.getInputStream());
				input = new BufferedReader(stream);
				output = new PrintWriter(socket.getOutputStream());
			}catch(IOException e) {
				e.printStackTrace();
			}

			output.println(Messages.CONNECTION_ESTABLISHED + clientCounter);
			output.flush();                                 //flush the output stream to make sure the message
			//was sent but not kept in the buffer (very important!)
			//get a message from the client
			while (true) {
				try {
					String msg;
					if ((msg = input.readLine()) != null) {
						String header = msg.split("\0")[0];
						String body = msg.split("\0")[1];
						if (Messages.compareHeaders(header, Messages.SET_USERNAME) && clientCounter == Lobby.PLAYERS_PER_GAME) {
							if (clientCounter == 1) { // make a new lobby
								lobbies.add(new Lobby());
								System.out.println(lobbies.size());
							}
							// add the last player before starting the game
							Lobby lobby = lobbies.get(lobbies.size() - 1);
							lobby.playerSockets[clientCounter - 1] = this;
							MazeGenerator mazeGenerator = new MazeGenerator();
							String maze = parseMaze(mazeGenerator.maze);
							System.out.println(maze);
							clientCounter = 0;
							for (int i = 0; i < lobby.playerSockets.length; i++) { // start game

								lobby.playerSockets[i].output.println(Messages.START_GAME + maze);
								lobby.playerSockets[i].output.flush();
							}
						} else if (Messages.compareHeaders(header, Messages.SET_USERNAME) && clientCounter < Lobby.PLAYERS_PER_GAME) {
							if (clientCounter == 1) { // make a new lobby
								lobbies.add(new Lobby());
								System.out.println(lobbies.size());
							}
							Lobby lobby = lobbies.get(lobbies.size() - 1);
							lobby.playerSockets[clientCounter - 1] = this;
						} else if (Messages.compareHeaders(header, Messages.MOVED_UP)) {
							Lobby lobby = lobbies.get(lobbies.size() - 1);
							for (int i = 0; i < lobby.playerSockets.length; i++) {
								lobby.playerSockets[i].output.println(Messages.MOVED_UP + body);
								lobby.playerSockets[i].output.flush();
							}
						} else if (Messages.compareHeaders(header, Messages.MOVED_DOWN)) {
							Lobby lobby = lobbies.get(lobbies.size() - 1);
							for (int i = 0; i < lobby.playerSockets.length; i++) {
								lobby.playerSockets[i].output.println(Messages.MOVED_DOWN + body);
								lobby.playerSockets[i].output.flush();
							}
						} else if (Messages.compareHeaders(header, Messages.MOVED_LEFT)) {
							Lobby lobby = lobbies.get(lobbies.size() - 1);
							for (int i = 0; i < lobby.playerSockets.length; i++) {
								lobby.playerSockets[i].output.println(Messages.MOVED_LEFT + body);
								lobby.playerSockets[i].output.flush();
							}
						} else if (Messages.compareHeaders(header, Messages.MOVED_RIGHT)) {
							Lobby lobby = lobbies.get(lobbies.size() - 1);
							for (int i = 0; i < lobby.playerSockets.length; i++) {
								lobby.playerSockets[i].output.println(Messages.MOVED_RIGHT + body);
								lobby.playerSockets[i].output.flush();
							}
						}
					}
				} catch (IOException e) {
					System.out.println("Failed to receive message from the client.");
					System.exit(1);
					e.printStackTrace();
				}
			}

		}
	}

	private static String parseMaze(float[][] maze) {

		String str = "";

		for (int i = 0; i < MazeGenerator.HEIGHT; i++) {
			for (int j = 0; j < MazeGenerator.WIDTH; j++) {
				if (maze[i][j] == 1.0f) {
					str += "1";
				} else {
					str += "0";
				}
			}
			str +="l";
		}

		return str;
	}

}