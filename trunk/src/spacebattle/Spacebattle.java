package spacebattle;


public class Spacebattle {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Model model = new Model();
		View view = new View(model);
		new Controller(model, view);
	}

}
