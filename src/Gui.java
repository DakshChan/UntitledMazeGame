import javax.swing.*;
import java.util.ArrayList;

public class Gui extends JFrame {
	int mapSizeX;
	int mapSizeY;
	
	float playerX;
	float playerY;
	
	private boolean[][] walls;
	private int[][] lighting;
	
	Gui(boolean[][] walls, int playerSpawnX, int playerSpawnY) {
		this.walls = walls;
		mapSizeX = this.walls.length;
		mapSizeY = this.walls[0].length;
		
		playerX = playerSpawnX;
		playerY = playerSpawnY;
		lighting = new int[mapSizeX][mapSizeY];
		
		this.setName("Untitled Maze Game");
		this.setSize(960,540);
		this.setResizable(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	
}
