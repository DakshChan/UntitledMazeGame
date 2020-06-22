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
import java.util.ArrayList;
import java.util.Arrays;

public class Client extends JFrame {

	int points = 0;
	JPanel currentPanel;
	Connection connection;

	int clientId;
	String lobbyId;

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
					connection.sendMsg(Messages.SET_USERNAME, username, lobbyId);

					//startGame(connection.maze);
					System.out.println("lobby");
					//showLobby();
					
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

		int[][] entityPos = new int[Lobby.PLAYERS_PER_GAME][3];

		//int playerX;
		//int playerY;
		//int playerDir; //0 Up, 1 Right, 2 Down, 3 Left
		
		int mapPosOffsetX;
		int mapPosOffsetY;

		private int[][] objects;
		private int[][] lighting;
		
		private BufferedImage IMGWallConnect;
		private BufferedImage IMGWallNub;
		private BufferedImage IMGWallStraight;
		private BufferedImage IMGFloor;
		private BufferedImage IMGNoise;
		private BufferedImage IMGPlayer;
		private BufferedImage IMGOrb;
		
		private BufferedImage map;
		
		private long lastMoveTime;
		private boolean movementEnabled;

		final int moveDelayMillis = 100;
		final int moveTolerance = 2;
		final int visibleTiles = 12;

		GamePanel(int[][] objects, int playerSpawnX, int playerSpawnY) {
			
			lastMoveTime = System.currentTimeMillis();
			movementEnabled = true;

			try{
				IMGWallConnect = ImageIO.read(new File("assets/connector.png"));
				IMGWallStraight = ImageIO.read(new File("assets/wall.png"));
				IMGWallNub = ImageIO.read(new File("assets/connectorNub.png"));
				IMGFloor = ImageIO.read(new File("assets/path.png"));
				IMGNoise = ImageIO.read(new File("assets/noise.png"));
				IMGPlayer = ImageIO.read(new File("assets/player.png"));
				IMGOrb = ImageIO.read(new File("assets/orb.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.requestFocus();
			this.setOpaque(true);
			this.objects = objects;
			mapSizeX = this.objects.length;
			mapSizeY = this.objects[0].length;

			connection.gamePanel = this;
			
			mapPosOffsetX = playerSpawnX;
			mapPosOffsetY = playerSpawnY;

			for (int i = 0; i < Lobby.PLAYERS_PER_GAME; i++) {
				entityPos[i][0] = playerSpawnX;
				entityPos[i][1] = playerSpawnY;
				entityPos[i][1] = 1;
			}

			lighting = new int[mapSizeX][mapSizeY];
			updateLighting();
			
			this.addKeyListener(this);
			
			//create map graphic
			map = new BufferedImage((int) (32*(mapSizeX + visibleTiles)), (int) (32*(mapSizeY + visibleTiles)),2);
			Graphics2D map2d = map.createGraphics();
			
			boolean left;
			boolean right;
			boolean up;
			boolean down;
			
			for (int x = 0; x < mapSizeX; x++) {
				for (int y = 0; y < mapSizeY; y++) {
					map2d.drawImage(IMGFloor, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
					if (objects[x][y] == 1) {
						left = false;
						right = false;
						up = false;
						down = false;
						
						if (x > 0 && x < mapSizeX - 1) {
							if (objects[x - 1][y] == 1) {
								left = true;
							}
							if (objects[x + 1][y] == 1) {
								right = true;
							}
						} else if (x == 0) {
							left = false;
							if (objects[x + 1][y] == 1) {
								right = true;
							}
						} else {
							right = false;
							if (objects[x - 1][y] == 1) {
								left = true;
							}
						}
						if (y > 0 && y < mapSizeY - 1) {
							if (objects[x][y - 1] == 1) {
								up = true;
							}
							if (objects[x][y + 1] == 1) {
								down = true;
							}
						} else if (y == 0) {
							up = false;
							if (objects[x][y + 1] == 1) {
								down = true;
							}
						} else {
							down = false;
							if (objects[x][y - 1] == 1) {
								up = true;
							}
						}
						
						if ((up && down) && !(left || right)) {
							map2d.drawImage(IMGWallStraight, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
						} else if ((left && right) && !(down || up)) {
							BufferedImage temp = new BufferedImage(32, 32, 2);
							
							Graphics2D temp2d = temp.createGraphics();
							temp2d.setColor(new Color(0, 0, 0, 0));
							temp2d.fillRect(0, 0, 32, 32);
							temp2d.rotate(Math.PI / 2, 32 / 2, 32 / 2);
							temp2d.drawImage(IMGWallStraight, 0, 0, null);
							temp2d.dispose();
							
							map2d.drawImage(temp, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
						} else {
							map2d.drawImage(IMGWallNub, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
							if (up) {
								map2d.drawImage(IMGWallConnect, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
							}
							if (down) {
								BufferedImage temp = new BufferedImage(32, 32, 2);
								
								Graphics2D temp2d = temp.createGraphics();
								temp2d.setColor(new Color(0, 0, 0, 0));
								temp2d.fillRect(0, 0, 32, 32);
								temp2d.rotate(Math.PI, 32 / 2, 32 / 2);
								temp2d.drawImage(IMGWallConnect, 0, 0, null);
								temp2d.dispose();
								
								map2d.drawImage(temp, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
							}
							if (left) {
								BufferedImage temp = new BufferedImage(32, 32, 2);
								
								Graphics2D temp2d = temp.createGraphics();
								temp2d.setColor(new Color(0, 0, 0, 0));
								temp2d.fillRect(0, 0, 32, 32);
								temp2d.rotate(3 * Math.PI / 2, 32 / 2, 32 / 2);
								temp2d.drawImage(IMGWallConnect, 0, 0, null);
								temp2d.dispose();
								
								map2d.drawImage(temp, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
							}
							if (right) {
								BufferedImage temp = new BufferedImage(32, 32, 2);
								
								Graphics2D temp2d = temp.createGraphics();
								temp2d.setColor(new Color(0, 0, 0, 0));
								temp2d.fillRect(0, 0, 32, 32);
								temp2d.rotate(Math.PI / 2, 32 / 2, 32 / 2);
								temp2d.drawImage(IMGWallConnect, 0, 0, null);
								temp2d.dispose();
								
								map2d.drawImage(temp, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
							}
						}
					}
				}
			}

			map2d.dispose();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setBackground(Color.BLACK);
			g2.clearRect(0,0,this.getWidth(),this.getHeight());
			
			BufferedImage mapClone = new BufferedImage(map.getWidth(), map.getHeight(), 2);
			Graphics2D map2d = mapClone.createGraphics();
			map2d.drawImage(map, 0,0,map.getWidth(),map.getHeight(),null);
		
			//Renders items and other changing entities
			for (int x = 0; x < mapSizeX; x++) {
				for (int y = 0; y < mapSizeY; y++) {
					if (objects[x][y] == 2) {
						map2d.drawImage(IMGOrb, x * 32 + (int) (visibleTiles / 2.0 * 32), y * 32 + (int) (visibleTiles / 2.0 * 32), null);
					}
				}
			}
			
			int playerX = entityPos[clientId - 1][0];
			int playerY = entityPos[clientId - 1][1];
			int playerDirection = entityPos[clientId - 1][2];
			
			//Renders player onto Map

			for (int i = 0; i < Lobby.PLAYERS_PER_GAME; i++) {
				BufferedImage temp = new BufferedImage(32,32,2);
				
				Graphics2D temp2d = temp.createGraphics();
				temp2d.setColor(new Color(0,0,0,0));
				temp2d.fillRect(0,0,32,32);
				temp2d.rotate(Math.PI/2 * entityPos[i][2], 32/2,32/2);
				float alpha;
				if (i == clientId-1) {
					alpha = 1.0f;
				} else {
					alpha = 0.5f;
				}
				AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
				temp2d.setComposite(alcom);
				temp2d.drawImage(IMGPlayer,0,0,null);
				temp2d.dispose();
				
				map2d.drawImage(temp, entityPos[i][0] * 32 + (int) (visibleTiles/2.0 * 32), entityPos[i][1] * 32 + (int) (visibleTiles/2.0 * 32),null);
			}
			
			
			for (int x = 0; x < mapSizeX; x++) {
				for (int y = 0; y < mapSizeY; y++) {
					if (lighting[x][y] > 0) {
						//Apply Lighting
						//IMGNoise should be turned more translucent the higher the light
						//int 5 should be max light or something
						
						BufferedImage temp = new BufferedImage(32,32,2);
						Graphics2D temp2d = temp.createGraphics();
						temp2d.setColor(new Color(0,0,0,0));
						temp2d.fillRect(0,0,32,32);
						AlphaComposite alcom = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) -(lighting[x][y]/13.0) + 1);
						temp2d.setComposite(alcom);
						temp2d.drawImage(IMGNoise,0,0,null);
						temp2d.dispose();
						
						map2d.drawImage(temp, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
					} else {
						map2d.setColor(new Color(0));
						map2d.fillRect(x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), 32,32);
					}
				}
			}
			
			//Gets rid of the 2d graphics
			map2d.dispose();
			
			//Replace with a properly scaled version based on player Pos
			//Instead of filling it to screen
			
			if (playerX - mapPosOffsetX > moveTolerance) {
				mapPosOffsetX ++;
			} else if (playerX - mapPosOffsetX < -moveTolerance) {
				mapPosOffsetX --;
			}
			if (playerY - mapPosOffsetY > moveTolerance) {
				mapPosOffsetY++;
			} else if (mapPosOffsetY - playerY > moveTolerance) {
				mapPosOffsetY--;
			}
			
			BufferedImage croppedMap = mapClone.getSubimage(mapPosOffsetX * 32, mapPosOffsetY * 32, visibleTiles*32, visibleTiles*32);
			
			
			int small = 0;
			if (this.getWidth() < this.getHeight()) {
				small = this.getWidth();
			} else {
				small = this.getHeight();
			}
			g2.drawImage(croppedMap,(this.getWidth() - small) / 2,(this.getHeight() - small) / 2, small, small, null);
		}
		
		private void updateLighting() {
			for (int i = 0; i < mapSizeX; i++) {
				for (int j = 0; j < mapSizeY; j++) {
					lighting[i][j] = 0;
				}
			}
			recursiveUpdateLighting(entityPos[clientId - 1][0], entityPos[clientId - 1][1],5);
		}

		private void recursiveUpdateLighting(int x, int y, int lightLevel) {
			if (lightLevel == 0) {
				return;
			}
			if (lighting[x][y] > lightLevel) {
				return;
			}
			lighting[x][y] = lightLevel;
			if (objects[x][y] == 1) {
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
			
			if (lastMoveTime + moveDelayMillis < System.currentTimeMillis() && movementEnabled) {
				if (code == 'w') {
					connection.sendMsg(Messages.MOVED_UP, Integer.toString(clientId), lobbyId);
				} else if (code == 's') {
					connection.sendMsg(Messages.MOVED_DOWN, Integer.toString(clientId), lobbyId);
				} else if (code == 'a') {
					connection.sendMsg(Messages.MOVED_LEFT, Integer.toString(clientId), lobbyId);
				} else if (code == 'd') {
					connection.sendMsg(Messages.MOVED_RIGHT, Integer.toString(clientId), lobbyId);
				}
			}

			repaint();

		}

		@Override
		public void keyReleased(KeyEvent e) {}

		private void movePlayerLeft(int id) {
			System.out.println("left");
			if (objects[entityPos[id - 1][0] - 1][entityPos[id - 1][1]] != 1) {
				if (objects[entityPos[id - 1][0] - 1][entityPos[id - 1][1]] == 2) {
					points += 100;
					objects[entityPos[id - 1][0] - 1][entityPos[id - 1][1]] = 0;
				}
				entityPos[id - 1][0] -= 1;
				updateLighting();
				lastMoveTime = System.currentTimeMillis();
			}


			entityPos[id - 1][2] = 3;
			repaint();
			if (checkEnd(entityPos[id - 1][0], entityPos[id - 1][1])) {
				movementEnabled = false;
				connection.sendMsg(Messages.FINISHED_MAZE, Integer.toString(id), lobbyId);
				JOptionPane.showMessageDialog(this, "Waiting for others to finish the maze.");
			}
		}

		private void movePlayerRight(int id) {
			if (objects[entityPos[id - 1][0] + 1][entityPos[id - 1][1]] != 1) {
				if (objects[entityPos[id - 1][0] + 1][entityPos[id - 1][1]] == 2) {
					points += 100;
					objects[entityPos[id - 1][0] + 1][entityPos[id - 1][1]] = 0;
				}
				entityPos[id - 1][0] += 1;
				updateLighting();
				lastMoveTime = System.currentTimeMillis();
			}

			entityPos[id - 1][2] = 1;
			repaint();
			if (checkEnd(entityPos[id - 1][0], entityPos[id - 1][1])) {
				movementEnabled = false;
				connection.sendMsg(Messages.FINISHED_MAZE, Integer.toString(id), lobbyId);
				JOptionPane.showMessageDialog(this, "Waiting for others to finish the maze.");
			}
		}

		private void movePlayerDown(int id) {
			if (objects[entityPos[id - 1][0]][entityPos[id - 1][1] + 1] != 1) {
				if (objects[entityPos[id - 1][0]][entityPos[id - 1][1] + 1] == 2) {
					points += 100;
					objects[entityPos[id - 1][0]][entityPos[id - 1][1] + 1] = 0;
				}
				entityPos[id - 1][1] += 1;
				updateLighting();
				lastMoveTime = System.currentTimeMillis();
			}
			entityPos[id - 1][2] = 2;

			repaint();
			if (checkEnd(entityPos[id - 1][0], entityPos[id - 1][1])) {
				movementEnabled = false;
				connection.sendMsg(Messages.FINISHED_MAZE, Integer.toString(id), lobbyId);
				JOptionPane.showMessageDialog(this, "Waiting for others to finish the maze.");
			}
		}

		private void movePlayerUp(int id) {
			if (objects[entityPos[id - 1][0]][entityPos[id - 1][1] - 1] != 1) {
				if (objects[entityPos[id - 1][0]][entityPos[id - 1][1] - 1] == 2) {
					points += 100;
					objects[entityPos[id - 1][0]][entityPos[id - 1][1] - 1] = 0;
				}
				entityPos[id - 1][1] -= 1;
				updateLighting();
				lastMoveTime = System.currentTimeMillis();
			}
			entityPos[id - 1][2] = 0;
			repaint();
			if (checkEnd(entityPos[id - 1][0], entityPos[id - 1][1])) {
				movementEnabled = false;
				connection.sendMsg(Messages.FINISHED_MAZE, Integer.toString(id), lobbyId);
				JOptionPane.showMessageDialog(this, "Waiting for others to finish the maze.");
			}
		}

		private boolean checkEnd(int x, int y) {
			return x == MazeGenerator.WIDTH - 3 && y == MazeGenerator.HEIGHT - 4;
		}

	}

	class LobbyPanel extends JPanel implements MouseListener {
		BufferedImage backdrop;
		ArrayList<String> names;
		
		LobbyPanel(ArrayList<String> names) {
			this.names = names;
			try {
				backdrop = ImageIO.read(new File("assets/UMG - Lobby.png"));
			} catch (IOException e) {
				e.printStackTrace();
				backdrop = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			}
			this.addMouseListener(this);
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(backdrop, 0, 0, this.getWidth(), this.getHeight(), this);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, (int) ((40/540.0) * this.getHeight())));
			int offsetX = (int) g.getFontMetrics().getStringBounds("Back to Lobby", g).getWidth()/2;
			int offsetY = (int) g.getFontMetrics().getStringBounds("Back to Lobby", g).getHeight();
			g.fillRoundRect((int) (((150-10)/960.0) * this.getWidth()) - offsetX, (int) (((450)/540.0) * this.getHeight()) - offsetY, offsetX*2 + (int) ((40/960.0) * this.getWidth()), (int) (((60)/540.0) * this.getHeight()),20,20);
			System.out.println((int) (((150-10)/960.0) * this.getWidth()) - offsetX);
			System.out.println((int) (((450)/540.0) * this.getHeight()) - offsetY);
			System.out.println(offsetX*2 + (int) ((40/960.0) * this.getWidth()));
			System.out.println( (int) (((60)/540.0) * this.getHeight()));
			
			g.setColor(Color.BLACK);
			g.drawString("Back to Lobby", (int) ((150/960.0) * this.getWidth()) - offsetX, (int) (((450)/540.0) * this.getHeight()));
			
			for (int i = 0; i < names.size(); i++) {
				g.setColor(Color.WHITE);
				g.setFont(new Font("Arial", Font.BOLD, (int) ((30/540.0) * this.getHeight())));
				offsetX = (int) g.getFontMetrics().getStringBounds(names.get(i), g).getWidth()/2;
				offsetY = (int) g.getFontMetrics().getStringBounds(names.get(i), g).getHeight()/2;
				g.fillRoundRect((int) (((480-10)/960.0) * this.getWidth()) - offsetX, (int) (((200+50*i-10)/540.0) * this.getHeight()) - offsetY, offsetX*2 + (int) ((20/960.0) * this.getWidth()), (int) (((30)/540.0) * this.getHeight()),10,10);
				g.setColor(Color.BLACK);
				g.drawString(names.get(i), (int) ((480/960.0) * this.getWidth()) - offsetX, (int) (((200+50*i)/540.0) * this.getHeight()));
			}

			if (names.size() == Lobby.PLAYERS_PER_GAME) {
				g.setColor(Color.WHITE);
				g.setFont(new Font("Arial", Font.BOLD, (int) ((30/540.0) * this.getHeight())));
				g.drawString("Starting Game in 5 Seconds", 500, 410);
			}

		}

		@Override
		public void mouseClicked(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getX() >= (10/960.0) * this.getWidth() && e.getX() <= (300/960.0) * this.getWidth()) {
				if (e.getY() >= (380/540.0) * this.getHeight() && e.getY() <= (435/540.0) * this.getHeight()) {
					connection.sendMsg(Messages.LEFT_LOBBY, Integer.toString(clientId), lobbyId);
					
					showMenu();
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

	class InstructionPanel extends JPanel implements MouseListener {
	BufferedImage backdrop;
	
	InstructionPanel() {
		try {
			backdrop = ImageIO.read(new File("assets/UMG - Instructions.png"));
		} catch (IOException e) {
			e.printStackTrace();
			backdrop = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
		}
		this.addMouseListener(this);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.drawImage(backdrop, 0, 0, this.getWidth(), this.getHeight(), this);
		
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
		if (e.getX() >= (340/960.0) * this.getWidth() && e.getX() <= (620/960.0) * this.getWidth()) {
			if (e.getY() >= (420/540.0) * this.getHeight() && e.getY() <= (480/540.0) * this.getHeight()) {
				showMenu();
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
	
	//This method should take in some variables
	void startGame(float[][] maze) {
		MazeGenerator g = new MazeGenerator(maze);
		int[][] walls = g.getMaze();
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
		currentPanel.requestFocus();

		revalidate();
		repaint();
	}

	void showLobby(ArrayList<String> names) {
		remove(currentPanel);
		currentPanel = new LobbyPanel(names);
		add(currentPanel);
		currentPanel.requestFocus();

		revalidate();
		repaint();
	}

	void updateLobby(ArrayList<String> names) {
		showLobby(names);
	}
	
	void showMenu() {
		remove(currentPanel);
		currentPanel = new MainMenuPanel();
		add(currentPanel);
		currentPanel.requestFocus();
		
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

		GamePanel gamePanel;
		
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
						System.out.println(header);
						if (Messages.compareHeaders(header, Messages.CONNECTION_ESTABLISHED)) {
							clientId = Integer.parseInt(body);
						} else if (Messages.compareHeaders(header, Messages.JOIN_LOBBY)) {
							lobbyId = body;
							showLobby(new ArrayList<String>());
						} else if (Messages.compareHeaders(header, Messages.UPDATE_LOBBY)) {
							updateLobby(parseNames(body));
						} else if (Messages.compareHeaders(header, Messages.START_GAME)) {
							startGame(parseMaze(body));
						} else if (Messages.compareHeaders(header, Messages.MOVED_UP)) {
							int id = Integer.parseInt(body);
							gamePanel.movePlayerUp(id);
						} else if (Messages.compareHeaders(header, Messages.MOVED_DOWN)) {
							int id = Integer.parseInt(body);
							gamePanel.movePlayerDown(id);
						} else if (Messages.compareHeaders(header, Messages.MOVED_LEFT)) {
							int id = Integer.parseInt(body);
							gamePanel.movePlayerLeft(id);
						} else if (Messages.compareHeaders(header, Messages.MOVED_RIGHT)) {
							int id = Integer.parseInt(body);
							gamePanel.movePlayerRight(id);
						} else if (Messages.compareHeaders(header, Messages.END_GAME)) {

						}
						
					}
				} catch (IndexOutOfBoundsException e) {
					System.out.println("Failed to receive message from the server.");
					e.printStackTrace();
					System.exit(1);
				} catch (IOException e) {
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
		
		public void sendMsg(String header, String body, String lobbyId) {
			output.println(header + body + "\0" + lobbyId);
			output.flush();
		}
		
	}

	private static ArrayList<String> parseNames(String names) {
		return new ArrayList<>(Arrays.asList(names.split("\t")));
	}

	private static float[][] parseMaze(String maze) {

		int row = 0;
		int column = 0;

		float[][] arr = new float[MazeGenerator.HEIGHT][MazeGenerator.WIDTH];

		for (int i = 0; i < maze.length(); i++) {
			char cell = maze.charAt(i);
			if (cell == '1') {
				arr[row][column] = 1.0f;
				column++;
			} else if (cell == '0') {
				arr[row][column] = 0.0f;
				column++;
			} else if (cell == 'l') {
				row++;
				column = 0;
			} else if (cell == '2') {
				arr[row][column] = 2.0f;
				column ++;
			}
		}

		return arr;
	}
}
