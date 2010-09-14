package spacebattle;

import lw3d.Lw3dView;

public class View extends Lw3dView {
	
	Model model;

	public View(Model model) {
		super(model);
		
		this.model = model;
	}

}
