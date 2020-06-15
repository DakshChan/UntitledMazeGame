public class Main {
	public static void main(String[] args) {
		boolean[][] walls = new boolean[][]{
				{true,true,true},
				{false,false,false},
				{false,false,false}};
		Gui g = new Gui(walls, 1,1);
	}
}
