import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
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
		private int[][] lighting;
		
		GamePanel(boolean[][] walls, int playerSpawnX, int playerSpawnY) {
			this.requestFocus();
			this.walls = walls;
			mapSizeX = this.walls.length;
			mapSizeY = this.walls[0].length;
			
			playerX = playerSpawnX;
			playerY = playerSpawnY;
			lighting = new int[mapSizeX][mapSizeY];
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setBackground(Color.BLUE);
			g2.setPaint(Color.BLACK);
			g2.drawRect(30,30,50,30);
		}
	}
	
	//This method should take in some variables
	void startGame() {
		
		//TEST FOR GUI
		boolean[][] walls = new boolean[][]{
				{true,true,true},
				{false,false,false},
				{false,false,false}};
		
		remove(currentPanel);
		currentPanel = new GamePanel(walls, 1, 1);
		add(currentPanel);
		
		revalidate();
		repaint();
	}
}
