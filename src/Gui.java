import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;

public class Gui extends JFrame {
	
	JPanel currentPanel;
	
	Gui() {
		currentPanel = new MainMenuPanel();
		this.add(currentPanel);
		
		this.setName("Untitled Maze Game");
		this.setSize(960,540);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	class MainMenuPanel extends JPanel implements MouseListener {
		BufferedImage menuImage;
		
		MainMenuPanel() {
			try {
				menuImage = ImageIO.read(new File("assets\\UMG - MainMenu.png"));
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
					System.out.println("play");
					
					startGame();
					
				} else if (e.getY() >= (350/540.0) * this.getHeight() && e.getY() <= (415/540.0) * this.getHeight()) {
					//INSTRUCT
					System.out.println("instruct");
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
	
	class GamePanel extends JPanel {
		private int mapSizeX;
		private int mapSizeY;
		
		float playerX;
		float playerY;
		
		private boolean[][] walls;
		private float[][] lighting;
		
		private BufferedImage IMGWallConnect;
		private BufferedImage IMGWallStraight;
		private BufferedImage IMGFloor;
		private BufferedImage IMGNoise;
		
		GamePanel(boolean[][] walls, int playerSpawnX, int playerSpawnY) {
			
			try{
				IMGWallConnect = ImageIO.read(new File("assets\\connector.png"));
				IMGWallStraight = ImageIO.read(new File("assets\\wall.png"));
				
				IMGFloor = ImageIO.read(new File("assets\\path.png"));
				IMGNoise = ImageIO.read(new File("assets\\noise.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.requestFocus();
			this.setOpaque(true);
			this.walls = walls;
			mapSizeX = this.walls.length;
			mapSizeY = this.walls[0].length;
			
			playerX = playerSpawnX;
			playerY = playerSpawnY;
			lighting = new float[mapSizeX][mapSizeY];
			for (int i = 0; i < mapSizeX; i++) {
				for (int j = 0; j < mapSizeY; j++) {
					lighting[i][j] = 1.0f;
				}
			}
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
			
			
			//Gets rid of the 2d graphics
			map2d.dispose();
			
			//Replace with a properly scaled version based on player Pos
			//Instead of filling it to screen
			g2.drawImage(map,0,0, this.getWidth(), this.getHeight(), null);
		}
	}
	
	//This method should take in some variables
	void startGame() {
		
		//TEST FOR GUI
		
		MazeGenerator g = new MazeGenerator();
		boolean[][] walls = g.getMaze();
		g.showMaze();
		
		remove(currentPanel);
		currentPanel = new GamePanel(walls, 1, 1);
		add(currentPanel);
		
		revalidate();
		repaint();
	}
}
