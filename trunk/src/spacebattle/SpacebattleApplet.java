package spacebattle;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.io.InputStream;

import lw3d.utils.GeometryLoader;
import lw3d.utils.StringLoader;
import lw3d.utils.TextureLoader;

public class SpacebattleApplet extends Applet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final private boolean fullscreen = false;
	
	Canvas displayParent;

	public void init() {
	}

	public void start() {
		setLayout(new BorderLayout());
		
		try {
			displayParent = new Canvas();
			displayParent.setSize(getWidth(), getHeight());
			add(displayParent);
			displayParent.setFocusable(true);
			displayParent.requestFocus();
			displayParent.setIgnoreRepaint(true);
			setVisible(true);
		} catch (Exception e) {
			System.err.println(e);
			throw new RuntimeException("Unable to create display."); 
		}

		try {
		GeometryLoader.setObject(this);
		StringLoader.setObject(this);
		TextureLoader.setObject(this);

		
			Model model = new Model(displayParent);
			View view = new View(model);
			new Controller(model, view);
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
