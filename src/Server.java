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
				//clientCounter = clientCounter + 1;
				//System.out.println("Client "+clientCounter+" connected");
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

//			output.println(Messages.CONNECTION_ESTABLISHED + clientCounter);
//			output.flush();
			while (true) {
				try {
					String msg;
					if ((msg = input.readLine()) != null) {
						String header = msg.split("\0")[0];
						String body = msg.split("\0")[1];
						String lobbyId = msg.split("\0")[2];
						if (Messages.compareHeaders(header, Messages.SET_USERNAME) && clientCounter == Lobby.PLAYERS_PER_GAME - 1) {
							clientCounter++;
							output.println(Messages.CONNECTION_ESTABLISHED + clientCounter);
							output.flush();
							if (clientCounter == 1) { // make a new lobby
								lobbies.add(new Lobby());
							}
							// add the last player before starting the game
							Lobby lobby = lobbies.get(lobbies.size() - 1);
							lobby.playerSockets[clientCounter - 1] = this;
							//lobby.names.add(body);
							/*String namesStr = "\\";
							for (String name : lobby.names) {
								namesStr += name + "\\";
							}*/
							output.println(Messages.JOIN_LOBBY + (lobbies.size() - 1));
							output.flush();
							MazeGenerator mazeGenerator = new MazeGenerator();
							String maze = parseMaze(mazeGenerator.maze);
							System.out.println(maze);
							clientCounter = 0;

							lobby.names.add(body);
							String namesStr = "";
							for (String name : lobby.names) {
								namesStr += name + "\t";
							}
							for (int i = 0; i < lobby.names.size(); i++) {
								lobby.playerSockets[i].output.println(Messages.UPDATE_LOBBY + namesStr);
								lobby.playerSockets[i].output.flush();
							}

							// delay
							try {
								Thread.sleep(5000);
							} catch (Exception e) {
								output.close();
								input.close();
								e.printStackTrace();
							}


							for (int i = 0; i < lobby.playerSockets.length; i++) { // start game

								lobby.playerSockets[i].output.println(Messages.START_GAME + maze);
								lobby.playerSockets[i].output.flush();
							}
						} else if (Messages.compareHeaders(header, Messages.SET_USERNAME) && clientCounter < Lobby.PLAYERS_PER_GAME) {
							clientCounter++;
							output.println(Messages.CONNECTION_ESTABLISHED + clientCounter);
							output.flush();
							if (clientCounter == 1) { // make a new lobby
								lobbies.add(new Lobby());
							}
							Lobby lobby = lobbies.get(lobbies.size() - 1);
							lobby.playerSockets[clientCounter - 1] = this;
							lobby.names.add(body);
							String namesStr = "";
							for (String name : lobby.names) {
								namesStr += name + "\t";
							}
							output.println(Messages.JOIN_LOBBY + (lobbies.size() - 1));

							for (int i = 0; i < lobby.names.size(); i++) {
								lobby.playerSockets[i].output.println(Messages.UPDATE_LOBBY + namesStr);
								lobby.playerSockets[i].output.flush();
							}

						} else if (Messages.compareHeaders(header, Messages.LEFT_LOBBY)) {
							Lobby lobby = lobbies.get(Integer.parseInt(lobbyId));
							lobby.names.remove(Integer.parseInt(body) - 1);
							clientCounter--;

							ConnectionHandler[] newArr = new ConnectionHandler[Lobby.PLAYERS_PER_GAME];
							for (int i = 0; i < lobby.playerSockets.length; i++) {
								if (i < Integer.parseInt(body) - 1) {
									newArr[i] = lobby.playerSockets[i];
								} else if (i > Integer.parseInt(body) - 1) {
									newArr[i - 1] = lobby.playerSockets[i];
								}
							}
							lobby.playerSockets = newArr;

							String namesStr = "";
							for (String name : lobby.names) {
								namesStr += name + "\t";
							}
							for (int i = 0; i < lobby.names.size(); i++) {
								lobby.playerSockets[i].output.println(Messages.UPDATE_LOBBY + namesStr);
								lobby.playerSockets[i].output.flush();
							}

						} else if (Messages.compareHeaders(header, Messages.MOVED_UP)) {
							Lobby lobby = lobbies.get(Integer.parseInt(lobbyId));
							for (int i = 0; i < lobby.playerSockets.length; i++) {
								lobby.playerSockets[i].output.println(Messages.MOVED_UP + body);
								lobby.playerSockets[i].output.flush();
							}
						} else if (Messages.compareHeaders(header, Messages.MOVED_DOWN)) {
							Lobby lobby = lobbies.get(Integer.parseInt(lobbyId));
							for (int i = 0; i < lobby.playerSockets.length; i++) {
								lobby.playerSockets[i].output.println(Messages.MOVED_DOWN + body);
								lobby.playerSockets[i].output.flush();
							}
						} else if (Messages.compareHeaders(header, Messages.MOVED_LEFT)) {
							Lobby lobby = lobbies.get(Integer.parseInt(lobbyId));
							for (int i = 0; i < lobby.playerSockets.length; i++) {
								lobby.playerSockets[i].output.println(Messages.MOVED_LEFT + body);
								lobby.playerSockets[i].output.flush();
							}
						} else if (Messages.compareHeaders(header, Messages.MOVED_RIGHT)) {
							Lobby lobby = lobbies.get(Integer.parseInt(lobbyId));
							for (int i = 0; i < lobby.playerSockets.length; i++) {
								lobby.playerSockets[i].output.println(Messages.MOVED_RIGHT + body);
								lobby.playerSockets[i].output.flush();
							}
						} else if (Messages.compareHeaders(header, Messages.FINISHED_MAZE)) {
							Lobby lobby = lobbies.get(Integer.parseInt(lobbyId));
							lobby.hasCompleted[Integer.parseInt(body) - 1] = true;
							if (everyoneHasEnded(lobby.hasCompleted)) {
								for (int i = 0; i < lobby.playerSockets.length; i++) {
									lobby.playerSockets[i].output.println(Messages.END_GAME + "scores");
									lobby.playerSockets[i].output.flush();
								}
							}

						}
					}
				} catch (IOException e) {
					System.out.println("Failed to receive message from the client.");
					e.printStackTrace();
					try {
						input.close();
						output.close();
					} catch (IOException err) {
						err.printStackTrace();
					}
					return;
				}
			}

		}
	}

	private static boolean everyoneHasEnded(boolean[] hasCompleted) {
		for (int i = 0; i < hasCompleted.length; i++) {
			if (!hasCompleted[i])
				return false;
		}
		return true;
	}

	private static String parseMaze(float[][] maze) {

		String str = "";

		for (int i = 0; i < MazeGenerator.HEIGHT; i++) {
			for (int j = 0; j < MazeGenerator.WIDTH; j++) {
				if (maze[i][j] == 1.0f) {
					str += "1";
				} else if (maze[i][j] == 2.0f){
					str += "2";
				} else {
					str += "0";
				}
			}
			str +="l";
		}

		return str;
	}

}