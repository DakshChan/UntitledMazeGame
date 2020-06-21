public class Entity {
	public int x, y;
	Direction direction;
	Type type;

	public enum Type {
		PLAYER,
		ENEMY
	}

	public enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT
	}
}
