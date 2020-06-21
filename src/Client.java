import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
	
	JPanel currentPanel;
	Connection connection;
	
	Client() {

		currentPanel = new MainMenuPanel();
		this.add(currentPanel);
		
		this.setName("Untitled Maze Game");
		this.setSize(960,540);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
		
		connection = new Connection();
		connection.go();

	}
	
	class MainMenuPanel extends JPanel implements MouseListener {
		BufferedImage menuImage;
		
		MainMenuPanel() {
			try {
				menuImage = ImageIO.read(new File("assets/UMG - MainMenu.png"));
			} catch (IOException e) {
				e.printStackTrace();
				menuImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			}
			
			this.addMouseListener(this);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(menuImage, 0, 0, this.getWidth(), this.getHeight(), this);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
		
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
		
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			System.out.println(e.getX() +" "+ e.getY());
			//960 , 540
			if (e.getX() >= (320/960.0) * this.getWidth() && e.getX() <= (620/960.0) * this.getWidth()) {
				if (e.getY() >= (280/540.0) * this.getHeight() && e.getY() <= (340/540.0) * this.getHeight()) {
					//PLAY

					String username = JOptionPane.showInputDialog(this, "Enter Username:");
					connection.sendMsg(Messages.SET_USERNAME + username);

					//startGame();
					
				} else if (e.getY() >= (350/540.0) * this.getHeight() && e.getY() <= (415/540.0) * this.getHeight()) {
					//INSTRUCT
					System.out.println("instruct");

					showInstructions();
				}
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
		
		}
		
		@Override
		public void mouseExited(MouseEvent e) {
		
		}
	}

	class GamePanel extends JPanel implements KeyListener {
		private int mapSizeX;
		private int mapSizeY;
		
		int playerX;
		int playerY;
		
		private boolean[][] walls;
		private float[][] lighting;
		
		private BufferedImage IMGWallConnect;
		private BufferedImage IMGWallNub;
		private BufferedImage IMGWallStraight;
		private BufferedImage IMGFloor;
		private BufferedImage IMGNoise;
		private BufferedImage IMGPlayer;
		
		GamePanel(boolean[][] walls, int playerSpawnX, int playerSpawnY) {
			
			try{
				IMGWallConnect = ImageIO.read(new File("assets/connector.png"));
				IMGWallStraight = ImageIO.read(new File("assets/wall.png"));
				IMGWallNub = ImageIO.read(new File("assets/connectorNub.png"));
				IMGFloor = ImageIO.read(new File("assets/path.png"));
				IMGNoise = ImageIO.read(new File("assets/noise.png"));
				IMGPlayer = ImageIO.read(new File("assets/player.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.requestFocus();
			this.setOpaque(true);
			this.walls = walls;
			mapSizeX = this.walls.length;
			mapSizeY = this.walls[0].length;
			
			playerX = playerSpawnX*360;
			playerY = playerSpawnY*360;

			lighting = new float[mapSizeX][mapSizeY];
			for (int i = 0; i < mapSizeX; i++) {
				for (int j = 0; j < mapSizeY; j++) {
					lighting[i][j] = 1.0f;
				}
			}

			this.addKeyListener(this);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setBackground(Color.BLACK);
			g2.clearRect(0,0,this.getWidth(),this.getHeight());
			
			BufferedImage map = new BufferedImage(mapSizeX * 320, mapSizeY * 320, 2);
			Graphics2D map2d = map.createGraphics();
			
			boolean left = false;
			boolean right = false;
			boolean up = false;
			boolean down = false;
			
			for (int x = 0; x < mapSizeX; x++) {
				for (int y = 0; y < mapSizeY; y++) {
					if (lighting[x][y] > 0) {
						map2d.drawImage(IMGFloor, x * 320, y * 320, null);
						if (walls[x][y] == true) {
							left = false;
							right = false;
							up = false;
							down = false;
							
							if (x > 0 && x < mapSizeX - 1) {
								if (walls[x-1][y] == true) {
									left = true;
								}
								if (walls[x+1][y] == true) {
									right = true;
								}
							} else if (x == 0) {
								left = false;
								if (walls[x+1][y] == true) {
									right = true;
								}
							} else {
								right = false;
								if (walls[x-1][y] == true) {
									left = true;
								}
							}
							if (y > 0 && y < mapSizeY - 1) {
								if (walls[x][y-1] == true) {
									up = true;
								}
								if (walls[x][y+1] == true) {
									down = true;
								}
							} else if (y == 0) {
								up = false;
								if (walls[x][y+1] == true) {
									down = true;
								}
							} else {
								down = false;
								if (walls[x][y-1] == true) {
									up = true;
								}
							}
							
							if ((up && down) && !(left || right)) {
								map2d.drawImage(IMGWallStraight, x * 320, y * 320, null);
							} else if ((left && right) && !(down || up)) {
								BufferedImage temp = new BufferedImage(320,320,2);
								
								Graphics2D temp2d = temp.createGraphics();
								temp2d.setColor(new Color(0,0,0,0));
								temp2d.fillRect(0,0,360,360);
								temp2d.rotate(Math.PI/2 , 160,160);
								temp2d.drawImage(IMGWallStraight,0,0,null);
								temp2d.dispose();
								
								map2d.drawImage(temp, x * 320, y * 320, null);
							} else {
								map2d.drawImage(IMGWallNub,x * 320,y * 320,null);
								if (up) {
									map2d.drawImage(IMGWallConnect, x * 320, y * 320, null);
								}
								if (down) {
									BufferedImage temp = new BufferedImage(320,320,2);
									
									Graphics2D temp2d = temp.createGraphics();
									temp2d.setColor(new Color(0,0,0,0));
									temp2d.fillRect(0,0,360,360);
									temp2d.rotate(Math.PI , 160,160);
									temp2d.drawImage(IMGWallConnect,0,0,null);
									temp2d.dispose();
									
									map2d.drawImage(temp, x * 320, y * 320, null);
								}
								if (left) {
									BufferedImage temp = new BufferedImage(320,320,2);
									
									Graphics2D temp2d = temp.createGraphics();
									temp2d.setColor(new Color(0,0,0,0));
									temp2d.fillRect(0,0,360,360);
									temp2d.rotate(3 * Math.PI/2, 160,160);
									temp2d.drawImage(IMGWallConnect,0,0,null);
									temp2d.dispose();
									
									map2d.drawImage(temp, x * 320, y * 320, null);
								}
								if (right) {
									BufferedImage temp = new BufferedImage(320,320,2);
									
									Graphics2D temp2d = temp.createGraphics();
									temp2d.setColor(new Color(0,0,0,0));
									temp2d.fillRect(0,0,360,360);
									temp2d.rotate(Math.PI/2 , 160,160);
									temp2d.drawImage(IMGWallConnect,0,0,null);
									temp2d.dispose();
									
									map2d.drawImage(temp, x * 320, y * 320, null);
								}
							}
						}
					} else {
						map2d.setPaint(Color.BLACK);
						map2d.drawRect(x * 320, y * 320, 320,320);
					}
				}
			}

			//ADD player rendering here
			//Each tile is 320px
			map2d.fillRect(playerX, playerY, 320, 320);

			
			//Gets rid of the 2d graphics
			map2d.dispose();
			
			//Replace with a properly scaled version based on player Pos
			//Instead of filling it to screen
			int small = 0;
			if (this.getWidth() < this.getHeight()) {
				small = this.getWidth();
			} else {
				small = this.getHeight();
			}
			
			
			g2.drawImage(map,(this.getWidth() - small) / 2,(this.getHeight() - small) / 2, small, small, null);
		}


		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyPressed(KeyEvent e) {
			char code = e.getKeyChar();

			if (code == 'w') {
				movePlayerUp();
			} else if (code == 's') {
				movePlayerDown();
			} else if (code == 'a') {
				movePlayerLeft();
			} else if (code == 'd') {
				movePlayerRight();
			}

			repaint();

		}

		@Override
		public void keyReleased(KeyEvent e) {

		}

		private void movePlayerLeft() {
			playerX -= 360;
		}

		private void movePlayerRight() {
			playerX += 360;
		}

		private void movePlayerDown() {
			playerY += 360;
		}

		private void movePlayerUp() {
			playerY -= 360;
		}

	}

	class InstructionPanel extends JPanel {


		InstructionPanel() {
			this.requestFocus();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.drawString("Hello World", 100, 100);
		}
	}
	
	//This method should take in some variables
	void startGame(float[][] maze) {
		
		//TEST FOR GUI
		
		MazeGenerator g = new MazeGenerator(maze);
		// TODO: Get Maze from server
		boolean[][] walls = g.getMaze();
		//g.showMaze();
		
		remove(currentPanel);
		currentPanel = new GamePanel(walls, 1, 1);
		add(currentPanel);

		currentPanel.requestFocus();
		revalidate();
		repaint();
	}


	void showInstructions() {
		remove(currentPanel);
		currentPanel = new InstructionPanel();
		add(currentPanel);

		revalidate();
		repaint();
	}
	
	class Connection {
		final String LOCAL_HOST = "127.0.0.1";
		final int PORT = 5000;
		
		Socket clientSocket;      //client socket for connection
		BufferedReader input;     //reader for the input stream
		PrintWriter output;       //writer for the output stream
		boolean running = true;   //program status
		
		boolean enteredGame = false;
		
		public void go() {
			
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
					if (input.ready()) {
						String msg = input.readLine();
						String header = msg.split("\0")[0];
						String body = msg.split("\0")[1];
						if (Messages.compareHeaders(header, Messages.START_GAME)) {
							startGame(parseMaze(body));
						}
						
					}
				}catch (Exception e) {
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

	private static float[][] parseMaze(String maze) {

		int row = 0;
		int column = 0;

		float[][] arr = new float[MazeGenerator.HEIGHT][MazeGenerator.WIDTH];

		for (int i = 0; i < maze.length(); i++) {
			char cell = maze.charAt(i);
			if (cell == '1') {
				System.out.println(row);
				System.out.println(column);
				arr[row][column] = 1.0f;
				column++;
			} else if (cell == '0') {
				arr[row][column] = 0.0f;
				column++;
			} else if (cell == 'l') {
				row++;
				column = 0;
			}
		}

		return arr;
	}
}
