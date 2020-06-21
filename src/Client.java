//imports for network communication
import java.io.*;
import java.net.*;

class Client {
	final String LOCAL_HOST = "127.0.0.1";
	final int PORT = 5000;

	Socket clientSocket;      //client socket for connection
	BufferedReader input;     //reader for the input stream
	PrintWriter output;       //writer for the output stream
	boolean running = true;   //program status

	public void go() {

		Gui g = new Gui(this);

		//create a socket (try-catch required) and attempt a connection to the local IP address
		System.out.println("Attempting to establish a connection ...");
		try {
			clientSocket = new Socket(LOCAL_HOST, PORT);    //create and bind a socket, and request connection
			InputStreamReader stream= new InputStreamReader(clientSocket.getInputStream());
			input = new BufferedReader(stream);
			output = new PrintWriter(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Connection to Server Failed");
			e.printStackTrace();
		}
		System.out.println("Connection to server established!");

		//output.println("Hi. I am a basic client!");         //send a message to the server
		//output.flush();                                     //flush the output stream to make sure the message
		//was sent but not kept in the buffer (very important!)
		//wait for response from the server
		while(running){
			try {
				if (input.ready()) {                        //check for an incoming message
					String msg = input.readLine();          //read the message
					System.out.println("Message from the server: " + msg);
				}
			}catch (IOException e) {
				System.out.println("Failed to receive message from the server.");
				e.printStackTrace();
			}
		}

		//after completing the communication close all streams and sockets
	/*	try {
			input.close();
			output.close();
			clientSocket.close();
		}catch (Exception e) {
			System.out.println("Failed to close stream or/and socket.");
		}*/
	}

	public void sendMsg(String msg) {
		output.println(msg);
		output.flush();
	}

}
