import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MazeGenerator {
	final static int HEIGHT = 19;
	final static int WIDTH = 19;

	enum Directions {
		UP,
		DOWN,
		LEFT,
		RIGHT
	}

	float[][] maze = new float[HEIGHT][WIDTH];

	MazeGenerator(float[][] maze) {
		this.maze = maze;
	}

	MazeGenerator() {
		for(int i = 0; i < HEIGHT; i++) {
			for(int j = 0; j < WIDTH; j++) {
				if (i % 2 == 1 || j % 2 == 1) {
					maze[i][j] = 0;
				}
				if (i == 0 || i == HEIGHT - 1 || j == 0 || j == WIDTH - 1) {
					maze[i][j] = 0.5f;
				}
			}
		}

		// an even random number from 2 to WIDTH - 2 exclusive
		Random random = new Random();
		int sx = 2 + random.nextInt((WIDTH - 4) / 2) * 2;
		int sy = 2 + random.nextInt((HEIGHT - 4) / 2) * 2;
		generator(sx, sy, maze);

		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				if (maze[i][j] == 0.5f) {
					maze[i][j] = 1.0f;
				}
			}
		}

		maze[HEIGHT - 2][WIDTH - 3] = 1.0f;
	}

	private void generator(int cx, int cy, float[][] grid) {
		grid[cy][cx] = 0.5f;

		Random random = new Random();

		if (grid[cy - 2][cx] == 0.5f && grid[cy + 2][cx] == 0.5f && grid[cy][cx - 2] == 0.5f && grid[cy][cx + 2] == 0.5f) {
			// has no where to go
		} else {
			ArrayList<Directions> directions = new ArrayList<>(Arrays.asList(Directions.UP, Directions.DOWN, Directions.LEFT, Directions.RIGHT));
			while (directions.size() > 0) {
				Directions direction = directions.get(random.nextInt(directions.size()));
				directions.remove(direction);

				int ny, nx, my, mx;

				if (direction == Directions.UP) {
					ny = cy - 2;
					my = cy - 1;
				} else if (direction == Directions.DOWN) {
					ny = cy + 2;
					my = cy + 1;
				} else {
					ny = cy;
					my = cy;
				}

				if (direction == Directions.LEFT) {
					nx = cx - 2;
					mx = cx - 1;
				} else if (direction == Directions.RIGHT) {
					nx = cx + 2;
					mx = cx + 1;
				} else {
					nx = cx;
					mx = cx;
				}

				if (grid[ny][nx] != 0.5f) {
					grid[my][mx] = 0.5f;
					generator(nx, ny, grid);
				}
			}
		}
	}

	public void showMaze() {
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				System.out.print(maze[i][j] + " ");
			}
			System.out.println("");
		}
	}

	public boolean[][] getMaze(){
		boolean[][] boolMaze = new boolean[WIDTH -2][HEIGHT-2];
		for (int i = 1; i < HEIGHT - 1; i++) {
			for (int j = 1; j < WIDTH - 1; j++) {
				boolMaze[i-1][j-1] = maze[i][j] != 1.0f;
			}
		}
		return boolMaze;
	}
}
