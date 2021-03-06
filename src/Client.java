import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


/**
 * Client program that connects to server and initiates maze game
 */
public class Client extends JFrame {
	int clientId;
	int playersPerGame;
	int mazeHeight;
	int mazeWidth;
	JPanel currentPanel;
	Connection connection;
	String lobbyId;

	/**
	 * Constructs client class, initiates connection and JFrame
	 */
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

	/**
	 * Main method, creates new client object.
	 */
	public static void main(String[] args) {
		Client player = new Client();
	}

	/**
	 * Main menu panel with splash screen and title.
	 */
	class MainMenuPanel extends JPanel implements MouseListener {

		/**
		 * Backdrop image
		 */
		BufferedImage menuImage;

		/**
		 * Constructs the menu panel, stores image as backdrop. Adds mouse listener.
		 */
		MainMenuPanel() {
			try {
				menuImage = ImageIO.read(new File("assets/UMG - MainMenu.png"));
			} catch (IOException e) {
				e.printStackTrace();
				menuImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			}
			
			this.addMouseListener(this);
		}

		/**
		 * paintComponent
		 * @param g the graphics component
		 */
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

		/**
		 * mouseReleased
		 * Retrieves mosue coordinates and takes action based on box clicked
		 * @param e mouse event
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			//960 , 540
			if (e.getX() >= (320/960.0) * this.getWidth() && e.getX() <= (620/960.0) * this.getWidth()) {
				if (e.getY() >= (280/540.0) * this.getHeight() && e.getY() <= (340/540.0) * this.getHeight()) {
					//PLAY

					String username = JOptionPane.showInputDialog(this, "Enter Username:");
					System.out.println("sent username");
					connection.sendMsg(Messages.SET_USERNAME, username, lobbyId);

					//startGame(connection.maze);
					//showLobby();
					
				} else if (e.getY() >= (350/540.0) * this.getHeight() && e.getY() <= (415/540.0) * this.getHeight()) {
					//INSTRUCTIONS
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

	/**
	 * Game panel with maze, player, game components.
	 */
	class GamePanel extends JPanel implements KeyListener {

		/**
		 * Horizontal and vertical map size
		 */
		private int mapSizeX;
		private int mapSizeY;

		/**
		 *
		 */
		int[][] entityPos = new int[playersPerGame][3];

		/**
		 *
		 */
		int mapPosOffsetX;
		int mapPosOffsetY;

		/**
		 *
		 */
		private int[][] objects;

		/**
		 *
		 */
		private int[][] lighting;

		/**
		 * Image assets
		 */
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

		final int moveDelayMillis = 150;
		final int moveTolerance = 4;
		final int visibleTiles = 12;

		/**
		 * Construct game panel with objects and player spawn coordinates
		 * @param objects 2D array of maze elements
		 * @param playerSpawnX Spawn x coordinate
		 * @param playerSpawnY Spawn y coordinate
		 */
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

			for (int i = 0; i < playersPerGame; i++) {
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

		/**
		 * Paint component of game panel. Displays all objects
		 * with lighting effects
		 * @param g Graphics object
		 */
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

			for (int i = 0; i < playersPerGame; i++) {
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
			
			BufferedImage lightingMap = new BufferedImage(map.getWidth(), map.getHeight(), 2);
			Graphics2D lighting2d = lightingMap.createGraphics();
			lighting2d.setColor(Color.BLACK);
			lighting2d.setBackground(new Color(0,0,0,0));
			lighting2d.fillRect(0,0,map.getWidth(), map.getHeight());
			
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
						
						lighting2d.clearRect(x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), 32,32);
						lighting2d.drawImage(temp, x * 32 + (int) (visibleTiles/2.0 * 32), y * 32 + (int) (visibleTiles/2.0 * 32), null);
					}
				}
			}
			lighting2d.dispose();
			
			map2d.drawImage(lightingMap, 0,0,map.getWidth(),map.getHeight(),null);
			
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

		/**
		 * Updates lighting of player's surroundings
		 */
		private void updateLighting() {
			for (int i = 0; i < mapSizeX; i++) {
				for (int j = 0; j < mapSizeY; j++) {
					lighting[i][j] = 0;
				}
			}
			recursiveUpdateLighting(entityPos[clientId - 1][0], entityPos[clientId - 1][1],5);
		}

		/**
		 * Recursive method of rendering lighting effects
		 * @param x
		 * @param y
		 * @param lightLevel
		 */
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

		/**
		 * Key event for movement
		 * @param e
		 */
		@Override
		public void keyPressed(KeyEvent e) {
			char code = e.getKeyChar();
			
			if (lastMoveTime + moveDelayMillis < System.currentTimeMillis() && movementEnabled) {
				if (code == 'w') {
					connection.sendMsg(Messages.MOVED_UP, Integer.toString(clientId), lobbyId);
					movePlayerUp(clientId);
					
				} else if (code == 's') {
					connection.sendMsg(Messages.MOVED_DOWN, Integer.toString(clientId), lobbyId);
					movePlayerDown(clientId);
					
				} else if (code == 'a') {
					connection.sendMsg(Messages.MOVED_LEFT, Integer.toString(clientId), lobbyId);
					movePlayerLeft(clientId);
					
				} else if (code == 'd') {
					connection.sendMsg(Messages.MOVED_RIGHT, Integer.toString(clientId), lobbyId);
					movePlayerRight(clientId);
				}
			}

			repaint();

		}

		@Override
		public void keyReleased(KeyEvent e) {}

		/**
		 * Move player left
		 * @param id ID of player
		 */
		private void movePlayerLeft(int id) {
			if (objects[entityPos[id - 1][0] - 1][entityPos[id - 1][1]] != 1) {
				if (objects[entityPos[id - 1][0] - 1][entityPos[id - 1][1]] == 2) {
					connection.sendMsg(Messages.PICKED_ITEM, Integer.toString(clientId), lobbyId);
					itemPickUpAudio(id);
					objects[entityPos[id - 1][0] - 1][entityPos[id - 1][1]] = 0;
				}
				entityPos[id - 1][0] -= 1;
				updateLighting();
				lastMoveTime = System.currentTimeMillis();
			}

			entityPos[id - 1][2] = 3;
			repaint();
			if (checkEnd(entityPos[id - 1][0], entityPos[id - 1][1]) && id == clientId) {
				movementEnabled = false;
				connection.sendMsg(Messages.FINISHED_MAZE, Integer.toString(id), lobbyId);
				JOptionPane.showMessageDialog(this.getParent(), "Waiting for others to finish the maze.");
			}
		}

		/**
		 * Move player right
		 * @param id ID of player
		 */
		private void movePlayerRight(int id) {
			if (objects[entityPos[id - 1][0] + 1][entityPos[id - 1][1]] != 1) {
				if (objects[entityPos[id - 1][0] + 1][entityPos[id - 1][1]] == 2) {
					connection.sendMsg(Messages.PICKED_ITEM, Integer.toString(clientId), lobbyId);
					itemPickUpAudio(id);
					objects[entityPos[id - 1][0] + 1][entityPos[id - 1][1]] = 0;
				}
				entityPos[id - 1][0] += 1;
				updateLighting();
				lastMoveTime = System.currentTimeMillis();
			}

			entityPos[id - 1][2] = 1;
			repaint();
			if (checkEnd(entityPos[id - 1][0], entityPos[id - 1][1]) && id == clientId) {
				movementEnabled = false;
				connection.sendMsg(Messages.FINISHED_MAZE, Integer.toString(id), lobbyId);
				JOptionPane.showMessageDialog(this.getParent(), "Waiting for others to finish the maze.");
			}
		}

		/**
		 * Move player down
		 * @param id ID of player
		 */
		private void movePlayerDown(int id) {
			if (objects[entityPos[id - 1][0]][entityPos[id - 1][1] + 1] != 1) {
				if (objects[entityPos[id - 1][0]][entityPos[id - 1][1] + 1] == 2) {
					connection.sendMsg(Messages.PICKED_ITEM, Integer.toString(clientId), lobbyId);
					itemPickUpAudio(id);
					objects[entityPos[id - 1][0]][entityPos[id - 1][1] + 1] = 0;
				}
				entityPos[id - 1][1] += 1;
				updateLighting();
				lastMoveTime = System.currentTimeMillis();
			}
			entityPos[id - 1][2] = 2;

			repaint();
			if (checkEnd(entityPos[id - 1][0], entityPos[id - 1][1]) && id == clientId) {
				movementEnabled = false;
				connection.sendMsg(Messages.FINISHED_MAZE, Integer.toString(id), lobbyId);
				JOptionPane.showMessageDialog(this.getParent(), "Waiting for others to finish the maze.");
			}
		}

		/**
		 * Move player up
		 * @param id ID of player
		 */
		private void movePlayerUp(int id) {
			if (objects[entityPos[id - 1][0]][entityPos[id - 1][1] - 1] != 1) {
				if (objects[entityPos[id - 1][0]][entityPos[id - 1][1] - 1] == 2) {
					connection.sendMsg(Messages.PICKED_ITEM, Integer.toString(clientId), lobbyId);
					itemPickUpAudio(id);
					objects[entityPos[id - 1][0]][entityPos[id - 1][1] - 1] = 0;
				}
				entityPos[id - 1][1] -= 1;
				updateLighting();
				lastMoveTime = System.currentTimeMillis();
			}
			entityPos[id - 1][2] = 0;
			repaint();
			if (checkEnd(entityPos[id - 1][0], entityPos[id - 1][1]) && id == clientId) {
				movementEnabled = false;
				connection.sendMsg(Messages.FINISHED_MAZE, Integer.toString(id), lobbyId);
				JOptionPane.showMessageDialog(this.getParent(), "Waiting for others to finish the maze.");
			}
		}

		/**
		 * Check if player's next mmove is the maze exit
		 * @param x x position of the player
		 * @param y y position of the player
		 * @return boolean for maze completion
		 */
		private boolean checkEnd(int x, int y) {
			return x == mazeWidth - 3 && y == mazeHeight - 4;
		}

		/**
		 * Play audio clip for item pickup
		 * @param id ID of player
		 */
		private void itemPickUpAudio(int id) {
			System.out.println("played audio");
			if (id == clientId) {
				try {
					File file = new File("assets/ItemPickUp.wav");
					Clip clip = AudioSystem.getClip();
					AudioInputStream ais = AudioSystem.getAudioInputStream(file);
					clip.open(ais);
					clip.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * Lobby panel that displays all players within current lobby
	 */
	class LobbyPanel extends JPanel implements MouseListener {

		/**
		 * Image for backdrop
		 */
		BufferedImage backdrop;

		/**
		 * ArrayList of current player names
		 */
		ArrayList<String> names;

		/**
		 * Boolean for ability to return to main menu
		 */
		boolean canGoBack;

		/**
		 * Constructs lobby panel with usernames
		 * @param names ArrayList of current players' usernames
		 */
		LobbyPanel(ArrayList<String> names) {
			this.names = names;
			canGoBack = true;
			try {
				backdrop = ImageIO.read(new File("assets/UMG - Lobby.png"));
			} catch (IOException e) {
				e.printStackTrace();
				backdrop = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			}
			this.addMouseListener(this);
		}

		/**
		 * Paint component of lobby panel
		 * @param g Graphics object
		 */
		@Override
		protected void paintComponent(Graphics g) {
			System.out.println(canGoBack);
			g.drawImage(backdrop, 0, 0, this.getWidth(), this.getHeight(), this);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, (int) ((40 / 540.0) * this.getHeight())));
			int offsetX = (int) g.getFontMetrics().getStringBounds("Back to Menu", g).getWidth() / 2;
			int offsetY = (int) g.getFontMetrics().getStringBounds("Back to Menu", g).getHeight();
			g.fillRoundRect((int) (((150 - 10) / 960.0) * this.getWidth()) - offsetX, (int) (((450) / 540.0) * this.getHeight()) - offsetY, offsetX * 2 + (int) ((40 / 960.0) * this.getWidth()), (int) (((60) / 540.0) * this.getHeight()), 20, 20);

				g.setColor(Color.BLACK);
				g.drawString("Back to Menu", (int) ((150 / 960.0) * this.getWidth()) - offsetX, (int) (((450) / 540.0) * this.getHeight()));
			
			for (int i = 0; i < names.size(); i++) {
				g.setColor(Color.WHITE);
				g.setFont(new Font("Arial", Font.BOLD, (int) ((30/540.0) * this.getHeight())));
				offsetX = (int) g.getFontMetrics().getStringBounds(names.get(i), g).getWidth()/2;
				offsetY = (int) g.getFontMetrics().getStringBounds(names.get(i), g).getHeight()/2;
				g.fillRoundRect((int) (((480-10)/960.0) * this.getWidth()) - offsetX, (int) (((200+50*i-10)/540.0) * this.getHeight()) - offsetY, offsetX*2 + (int) ((20/960.0) * this.getWidth()), (int) (((30)/540.0) * this.getHeight()),10,10);
				g.setColor(Color.BLACK);
				g.drawString(names.get(i), (int) ((480/960.0) * this.getWidth()) - offsetX, (int) (((200+50*i)/540.0) * this.getHeight()));
			}

			if (names.size() == playersPerGame) {
				canGoBack = false;
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

		/**
		 * Mouse event for returning to main menu only if back button is allowed,
		 * only when game has not started
		 * @param e Mouse event
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			if (canGoBack) {
				if (e.getX() >= (10 / 960.0) * this.getWidth() && e.getX() <= (300 / 960.0) * this.getWidth()) {
					if (e.getY() >= (380 / 540.0) * this.getHeight() && e.getY() <= (435 / 540.0) * this.getHeight()) {
						connection.sendMsg(Messages.LEFT_LOBBY, Integer.toString(clientId), lobbyId);

						showMenu();
					}
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

	/**
	 * Leaderboard panel that displays leaderboard
	 */
	class LeaderboardPanel extends JPanel implements MouseListener {

		/**
		 * Backdrop image
		 */
		BufferedImage backdrop;

		/**
		 * Scores of all players
		 */
		ArrayList<String> scores;

		/**
		 * Constructs panel with player scores
		 * @param scores ArrayList of scores
		 */
		LeaderboardPanel(ArrayList<String> scores) {
			this.scores = scores;
			try {
				backdrop = ImageIO.read(new File("assets/UMG - Leaderboard.png"));
			} catch (IOException e) {
				e.printStackTrace();
				backdrop = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			}
			this.addMouseListener(this);
		}

		/**
		 * Paint component of leaderboard panel
		 * @param g Graphics object
		 */
		@Override
		protected void paintComponent(Graphics g) {
			g.drawImage(backdrop, 0, 0, this.getWidth(), this.getHeight(), this);
			g.setColor(Color.WHITE);
			g.setFont(new Font("Arial", Font.BOLD, (int) ((40/540.0) * this.getHeight())));
			int offsetX = (int) g.getFontMetrics().getStringBounds("Back to Menu", g).getWidth()/2;
			int offsetY = (int) g.getFontMetrics().getStringBounds("Back to Menu", g).getHeight();
			g.fillRoundRect((int) (((150-10)/960.0) * this.getWidth()) - offsetX, (int) (((450)/540.0) * this.getHeight()) - offsetY, offsetX*2 + (int) ((40/960.0) * this.getWidth()), (int) (((60)/540.0) * this.getHeight()),20,20);

			g.setColor(Color.BLACK);
			g.drawString("Back to Menu", (int) ((150/960.0) * this.getWidth()) - offsetX, (int) (((450)/540.0) * this.getHeight()));

			for (int i = 0; i < scores.size(); i++) {
				g.setColor(Color.WHITE);
				g.setFont(new Font("Arial", Font.BOLD, (int) ((30/540.0) * this.getHeight())));
				offsetX = (int) g.getFontMetrics().getStringBounds((i + 1) + ". " + scores.get(i), g).getWidth()/2;
				offsetY = (int) g.getFontMetrics().getStringBounds((i + 1) + ". " + scores.get(i), g).getHeight()/2;
				g.fillRoundRect((int) (((480-10)/960.0) * this.getWidth()) - offsetX, (int) (((200+50*i-10)/540.0) * this.getHeight()) - offsetY, offsetX*2 + (int) ((20/960.0) * this.getWidth()), (int) (((30)/540.0) * this.getHeight()),10,10);
				g.setColor(Color.BLACK);
				g.drawString((i + 1) + ". " + scores.get(i), (int) ((480/960.0) * this.getWidth()) - offsetX, (int) (((200+50*i)/540.0) * this.getHeight()));
			}

		}

		@Override
		public void mouseClicked(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		/**
		 * Mouse event for going back to menu
		 * @param e Mouse Event
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.getX() >= (10/960.0) * this.getWidth() && e.getX() <= (300/960.0) * this.getWidth()) {
				if (e.getY() >= (380/540.0) * this.getHeight() && e.getY() <= (435/540.0) * this.getHeight()) {
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

	/**
	 * Instruction panel with guide to game mechanics, goal and controls
	 */
	class InstructionPanel extends JPanel implements MouseListener {

		/**
		 * Back drop image
		 */
		BufferedImage backdrop;

		/**
		 * Constructs instruction panel
		 */
		InstructionPanel() {
			try {
				backdrop = ImageIO.read(new File("assets/UMG - Instructions.png"));
			} catch (IOException e) {
				e.printStackTrace();
				backdrop = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
			}
			this.addMouseListener(this);
		}

		/**
		 * Paint component of instruction panel
		 * @param g Graphics object
		 */
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

		/**
		 * Mouse event checker for returning to menu
		 * @param e Mouse Event
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
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

	/**
	 * startGame
	 * Begins the game with a maze and game panel
	 * @param maze Float array of values for maze elements
	 */
	void startGame(float[][] maze, int height, int width) {

		/**
		 * Maze generator
		 */
		MazeGenerator g = new MazeGenerator(maze, height, width);

		/**
		 * 2D array of walls
		 */
		int[][] walls = g.getMaze();

		// Remove current panel and intiate game panel
		remove(currentPanel);
		currentPanel = new GamePanel(walls, 1, 1);
		add(currentPanel);
		currentPanel.requestFocus();
		
		revalidate();
		repaint();
	}

	/**
	 * showInstructions
	 * Shows the instructions page
	 */
	void showInstructions() {
		remove(currentPanel);
		currentPanel = new InstructionPanel();
		add(currentPanel);
		currentPanel.requestFocus();

		revalidate();
		repaint();
	}

	/**
	 * showLobby
	 * Shows the lobby panel with current players
	 */
	void showLobby(ArrayList<String> names) {
		remove(currentPanel);
		currentPanel = new LobbyPanel(names);
		add(currentPanel);
		currentPanel.requestFocus();

		revalidate();
		repaint();
	}


	/**
	 * updateLobby
	 * Updates the lobby with a new list of players
	 * @param names ArrayList of player names
	 */
	void updateLobby(ArrayList<String> names) {
		showLobby(names);
	}

	/**
	 * showLeaderboard
	 * Shows leaderboard panel
	 * @param scores ArrayList of scores
	 */
	void showLeaderboard(ArrayList<String> scores) {
		remove(currentPanel);
		currentPanel = new LeaderboardPanel(scores);
		add(currentPanel);
		currentPanel.requestFocus();

		revalidate();
		repaint();

	}

	/**
	 * showMenu
	 * Shows main menu panel
	 */
	void showMenu() {
		remove(currentPanel);
		currentPanel = new MainMenuPanel();
		add(currentPanel);
		currentPanel.requestFocus();
		
		revalidate();
		repaint();
	}

	/**
	 * Connection with server
	 */
	class Connection {
		//final String LOCAL_HOST = "daksh.asuscomm.com";
		final String HOST = "127.0.0.1";
		final int PORT = 5000;
		
		Socket clientSocket;      //client socket for connection
		BufferedReader input;     //reader for the input stream
		PrintWriter output;       //writer for the output stream
		boolean running = true;   //program status

		GamePanel gamePanel;	  // a reference to the gamePanel object
		
		public void go() {
			
			//create a socket (try-catch required) and attempt a connection to the local IP address
			System.out.println("Attempting to establish a connection ...");
			try {
				clientSocket = new Socket(HOST, PORT);    //create and bind a socket, and request connection
				InputStreamReader stream= new InputStreamReader(clientSocket.getInputStream());
				input = new BufferedReader(stream);
				output = new PrintWriter(clientSocket.getOutputStream());
			} catch (IOException e) {
				System.out.println("Connection to Server Failed");
				e.printStackTrace();
			}
			System.out.println("Connection to server established!");
			
			while(running){
				try {
					if (input.ready()) {
						// parse the message received from the server
						String msg = input.readLine();
						String header = msg.split("\0")[0];
						String body = msg.split("\0")[1];
						System.out.println("h"+header);
						System.out.println("b"+body);
						if (Messages.compareHeaders(header, Messages.CONNECTION_ESTABLISHED)) {
							ArrayList<Integer> configs = parseConfig(body);
							clientId = configs.get(0);
							playersPerGame = configs.get(1);
							mazeHeight = configs.get(2);
							mazeWidth = configs.get(3);
						} else if (Messages.compareHeaders(header, Messages.JOIN_LOBBY)) {
							// set the lobbyId to the lobbyId that the server set
							lobbyId = body;
							showLobby(new ArrayList<String>());
						} else if (Messages.compareHeaders(header, Messages.UPDATE_LOBBY)) {
							// update the lobby when a new player joins. body is the name of the players in the game
							updateLobby(parseNames(body));
						} else if (Messages.compareHeaders(header, Messages.START_GAME)) {
							// start the game where body is the string representation of the maze
							startGame(parseMaze(body, mazeHeight, mazeWidth),mazeHeight,mazeWidth);
						} else if (Messages.compareHeaders(header, Messages.MOVED_UP)) {
							int id = Integer.parseInt(body);
							if (id != clientId)
								gamePanel.movePlayerUp(id);
						} else if (Messages.compareHeaders(header, Messages.MOVED_DOWN)) {
							int id = Integer.parseInt(body);
							if (id != clientId)
								gamePanel.movePlayerDown(id);
						} else if (Messages.compareHeaders(header, Messages.MOVED_LEFT)) {
							int id = Integer.parseInt(body);
							if (id != clientId)
								gamePanel.movePlayerLeft(id);
						} else if (Messages.compareHeaders(header, Messages.MOVED_RIGHT)) {
							int id = Integer.parseInt(body);
							if (id != clientId)
								gamePanel.movePlayerRight(id);
						} else if (Messages.compareHeaders(header, Messages.END_GAME)) {
							// show the leaderboard. body is a string representation of the scores
							showLeaderboard(parseScores(body));
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

		try {
			input.close();
			output.close();
			clientSocket.close();
		} catch (Exception e) {
			System.out.println("Failed to close stream or/and socket.");
		}
		
		}

		/**
		 * Send a message from the client to the server
		 * @param header the header of the message chosen from Messages class
		 * @param body the body of the message
		 * @param lobbyId the lobbyId of the current game
		 */
		public synchronized void sendMsg(String header, String body, String lobbyId) {
			System.out.println(header + body + "\0" + lobbyId);
			output.println(header + body + "\0" + lobbyId);
			output.flush();
		}
		
	}

	private static ArrayList<Integer> parseConfig(String config) {
		ArrayList<Integer> configArr = new ArrayList<>();
		System.out.println(config);
		String[] configs = config.split("\t");

		configArr.add(Integer.parseInt(configs[0])); // client id
		configArr.add(Integer.parseInt(configs[1])); // players_per_game
		configArr.add(Integer.parseInt(configs[2])); // height
		configArr.add(Integer.parseInt(configs[3])); // width

		return configArr;
	}

	/**
	 * parseNames parses the string representation of the names sent by the server in the body of UPDATE_LOBBY and JOIN_LOBBY
	 * description parsing convention is based on the character "\t"
	 * @param names the string representation
	 * @return ArrayList the names in an ArrayList
	 */
	private static ArrayList<String> parseNames(String names) {
		return new ArrayList<>(Arrays.asList(names.split("\t")));
	}

	/**
	 * parseScores parses the string representation of the scores sent by the server in the body of END_GAME
	 * description parsing convention is based on character "\t"
	 * @param scores the string representation
	 * @return ArrayList the scores in ArrayList
	 */
	private static ArrayList<String> parseScores(String scores) {
		System.out.println(scores);
		ArrayList<String> scoresArr = new ArrayList<>(Arrays.asList(scores.split("\t")));
		Collections.reverse(scoresArr);
		for (String scr : scoresArr) {
			System.out.println(scr);
		}
		return scoresArr;
	}

	/**
	 * parseMaze
	 * Parses the server-generated string into a 2D float array, with
	 * values representing maze elements
	 * @param maze Server-provided string of maze layout
	 * @return float[][] 2D array of maze elements
	 */
	private static float[][] parseMaze(String maze, int height, int width) {

		int row = 0;
		int column = 0;

		float[][] arr = new float[height][width];

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
