import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
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

					//startGame(connection.maze);
					
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
		int playerDir; //0 Up, 1 Right, 2 Down, 3 Left
		
		private boolean[][] walls;
		private int[][] lighting;
		
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
			
			playerX = playerSpawnX*320;
			playerY = playerSpawnY*320;
			
			

			lighting = new int[mapSizeX][mapSizeY];
			recursiveUpdateLighting(playerSpawnX, playerSpawnY, 5);
			
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
								temp2d.fillRect(0,0,320,320);
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
									temp2d.fillRect(0,0,320,320);
									temp2d.rotate(Math.PI , 160,160);
									temp2d.drawImage(IMGWallConnect,0,0,null);
									temp2d.dispose();
									
									map2d.drawImage(temp, x * 320, y * 320, null);
								}
								if (left) {
									BufferedImage temp = new BufferedImage(320,320,2);
									
									Graphics2D temp2d = temp.createGraphics();
									temp2d.setColor(new Color(0,0,0,0));
									temp2d.fillRect(0,0,320,320);
									temp2d.rotate(3 * Math.PI/2, 160,160);
									temp2d.drawImage(IMGWallConnect,0,0,null);
									temp2d.dispose();
									
									map2d.drawImage(temp, x * 320, y * 320, null);
								}
								if (right) {
									BufferedImage temp = new BufferedImage(320,320,2);
									
									Graphics2D temp2d = temp.createGraphics();
									temp2d.setColor(new Color(0,0,0,0));
									temp2d.fillRect(0,0,320,320);
									temp2d.rotate(Math.PI/2 , 160,160);
									temp2d.drawImage(IMGWallConnect,0,0,null);
									temp2d.dispose();
									
									map2d.drawImage(temp, x * 320, y * 320, null);
								}
							}
						}
						//Apply Lighting
						//IMGNoise should be turned more translucent the higher the light
						//int 5 should be max light or something
						
						BufferedImage temp = new BufferedImage(320,320,2);
						Graphics2D temp2d = temp.createGraphics();
						temp2d.setColor(new Color(0,0,0,0));
						temp2d.fillRect(0,0,320,320);
						AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) -(lighting[x][y]/5.0) + 1);
						temp2d.setComposite(alcom);
						temp2d.drawImage(IMGNoise,0,0,null);
						temp2d.dispose();
						
						map2d.drawImage(temp, x * 320, y * 320, null);
						
					} else {
						map2d.setPaint(Color.BLACK);
						map2d.drawRect(x * 320, y * 320, 320,320);
					}
				}
			}
			
			//Renders player onto Map
			BufferedImage temp = new BufferedImage(320,320,2);
			
			Graphics2D temp2d = temp.createGraphics();
			temp2d.setColor(new Color(0,0,0,0));
			temp2d.fillRect(0,0,320,320);
			temp2d.rotate(Math.PI/2 * playerDir, 160,160);
			temp2d.drawImage(IMGPlayer,0,0,null);
			temp2d.dispose();
			
			map2d.drawImage(temp, playerX, playerY, null);
			
			//Gets rid of the 2d graphics
			map2d.dispose();
			
			
			int small = 0;
			if (this.getWidth() < this.getHeight()) {
				small = this.getWidth();
			} else {
				small = this.getHeight();
			}
			
			//Replace with a properly scaled version based on player Pos
			//Instead of filling it to screen
			
			g2.drawImage(map,(this.getWidth() - small) / 2,(this.getHeight() - small) / 2, small, small, null);
		}
		
		private void recursiveUpdateLighting(int x, int y, int lightLevel) {
			if (lightLevel == 0) {
				return;
			}
			if (lighting[x][y] > lightLevel) {
				return;
			}
			lighting[x][y] = lightLevel;
			if (walls[x][y] == true) {
				return;
			}
			if (x > 0) {
				recursiveUpdateLighting(x-1, y, lightLevel-1);
			}
			if (x < mapSizeX-1) {
				recursiveUpdateLighting(x + 1, y, lightLevel-1);
			}
			if (y > 0) {
				recursiveUpdateLighting(x, y + 1, lightLevel - 1);
			}
			if (y < mapSizeY-1) {
				recursiveUpdateLighting(x, y - 1, lightLevel - 1);
			}
			if (x > 0 && y > 0) {
				recursiveUpdateLighting(x-1, y-1, lightLevel-2);
			}
			if (x < mapSizeX-1 && y > 0) {
				recursiveUpdateLighting(x+1, y-1, lightLevel-2);
			}
			if (x > 0 && y < mapSizeY-1) {
				recursiveUpdateLighting(x-1, y+1, lightLevel-2);
			}
			if (x < mapSizeX-1 && y < mapSizeY-1) {
				recursiveUpdateLighting(x+1, y+1, lightLevel-2);
			}
		}
		
		@Override
		public void keyTyped(KeyEvent e) {}

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
		public void keyReleased(KeyEvent e) {}

		private void movePlayerLeft() {
			int playerXIndex = (int) Math.floor(playerX/320.0);
			int playerYIndex = (int) Math.floor(playerY/320.0);
			if (walls[playerXIndex - 1][playerYIndex] != true) {
				playerX -= 320;
				for (int i = 0; i < mapSizeX; i++) {
					for (int j = 0; j < mapSizeY; j++) {
						lighting[i][j] = 0;
					}
				}
				recursiveUpdateLighting(playerXIndex-1, playerYIndex,5);
			}
			playerDir = 3;
		}

		private void movePlayerRight() {
			int playerXIndex = (int) Math.floor(playerX/320.0);
			int playerYIndex = (int) Math.floor(playerY/320.0);
			if (walls[playerXIndex + 1][playerYIndex] != true) {
				playerX += 320;
				for (int i = 0; i < mapSizeX; i++) {
					for (int j = 0; j < mapSizeY; j++) {
						lighting[i][j] = 0;
					}
				}
				recursiveUpdateLighting(playerXIndex+1, playerYIndex,5);
			}
			playerDir = 1;
		}

		private void movePlayerDown() {
			int playerXIndex = (int) Math.floor(playerX/320.0);
			int playerYIndex = (int) Math.floor(playerY/320.0);
			if (walls[playerXIndex][playerYIndex + 1] != true) {
				playerY += 320;
				for (int i = 0; i < mapSizeX; i++) {
					for (int j = 0; j < mapSizeY; j++) {
						lighting[i][j] = 0;
					}
				}
				recursiveUpdateLighting(playerXIndex, playerYIndex+1,5);
			}
			playerDir = 2;
		}

		private void movePlayerUp() {
			int playerXIndex = (int) Math.floor(playerX/320.0);
			int playerYIndex = (int) Math.floor(playerY/320.0);
			if (walls[playerXIndex][playerYIndex - 1] != true) {
				playerY -= 320;
				for (int i = 0; i < mapSizeX; i++) {
					for (int j = 0; j < mapSizeY; j++) {
						lighting[i][j] = 0;
					}
				}
				recursiveUpdateLighting(playerXIndex, playerYIndex-1,5);
			}
			playerDir = 0;
		}
	}

	class InstructionPanel extends JPanel {
		
		InstructionPanel() {
			this.requestFocus();
		}

		@Override
		protected void paintComponent(Graphics g) {
			int width = this.getWidth();
			int height = this.getHeight();

			// graphics setup
			Graphics2D g2 = (Graphics2D) g;
			RenderingHints renderingHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHints(renderingHints);

			// title
			Font bigFont = new Font("Arial", Font.BOLD, 36);
			g2.setFont(bigFont);
			FontMetrics fontMetrics = g2.getFontMetrics();
			String instructionTitle = "Instructions";
			int titleStartY = (int)(height * 0.2);
			g2.drawString(instructionTitle, width/2 - fontMetrics.stringWidth(instructionTitle)/2, titleStartY);

			// body
			Font bodyFont = new Font("Arial", Font.PLAIN, 18);
			g2.setFont(bodyFont);
			fontMetrics = g2.getFontMetrics();

			String[] instructionTextLines = {
					"Every player is sent the same maze and must race to find the exit",
					"Players are ranked by completion time",
					"The maze is dark but players will have a flashlight to help them see"
			};

			int bodyStartX = (int)(width * 0.25f);
			int bodyStartY = (int)(height * 0.30f);

			for (int i = 0; i < instructionTextLines.length; i++) {
				g2.drawString(instructionTextLines[i], bodyStartX, bodyStartY + (int)(i * fontMetrics.getHeight() * 1.25));
			}
		}
	}
	
	//This method should take in some variables
	void startGame(float[][] maze) {
		
		//TEST FOR GUI
		
		MazeGenerator g = new MazeGenerator(maze);
		// TODO: Get Maze from server
		boolean[][] walls = g.getMaze();
		g.showMaze();
		
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
		
		public float[][] maze;
		
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
							maze = parseMaze(body);
							System.out.println("huh?!");
							startGame(maze);
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
