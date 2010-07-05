package spacebattle;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.io.InputStream;

import lw3d.utils.GeometryLoader;
import lw3d.utils.StringLoader;
import lw3d.utils.TextureLoader;

public class ExampleApplet extends Applet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final private boolean fullscreen = false;

	Example test;

	public void init() {
	}

	public void start() {
		setLayout(new BorderLayout());
		Canvas displayParent = new Canvas();
		displayParent.setSize(getWidth(), getHeight());
		add(displayParent);

		GeometryLoader.setObject(this);
		StringLoader.setObject(this);
		TextureLoader.setObject(this);

		Model model = new Model(displayParent);
		View view = new View(model);
		new Controller(model, view);
	}

}
