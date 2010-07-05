package spacebattle;


public class Example {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Model model = new Model();
		View view = new View(model);
		new Controller(model, view);
	}

}
