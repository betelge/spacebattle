package spacebattle;

import java.util.Iterator;

import org.lwjgl.input.Keyboard;

import lw3d.Lw3dController;
import lw3d.Lw3dModel;
import lw3d.Lw3dView;
import lw3d.math.Quaternion;
import lw3d.math.Vector3f;

public class Controller extends Lw3dController {
	
	Model model;
	View view;

	public Controller(Model model, View view) {
		super(model, view);
		
		this.model = model;
		this.view = view;
	}

	@Override
	protected void onMouseMove(int dX, int dY, int x, int y) {
		Quaternion rot = new Quaternion().fromAngleNormalAxis(-dX * 0.01f,
				Vector3f.UNIT_Y);
		rot.multThis(new Quaternion().fromAngleNormalAxis(dY * 0.01f,
				Vector3f.UNIT_X));
		model.getCameraNode().getTransform().getRotation().multThis(rot);
	}

	@Override
	protected void onMouseWheel(int dWheel, int x, int y) {
		Quaternion rot = new Quaternion().fromAngleNormalAxis(dWheel * 0.001f,
				Vector3f.UNIT_Z);
		model.getCameraNode().getTransform().getRotation().multThis(rot);
	}

	@Override
	protected void onMouseButton(int button, boolean buttonState, int x, int y) {
		if (buttonState)
			System.out.println("Click: (" + x + ", " + y + ")");
	}

	@Override
	protected void onKey(int key, boolean isDown, boolean repeatEvent) {

		if (!repeatEvent) {
			Vector3f vector = new Vector3f();
			float speed = 0.15f;

			Iterator<Integer> it = model.getKeys().iterator();
			while (it.hasNext()) {
				switch (it.next()) {
				case Keyboard.KEY_UP:
				case Keyboard.KEY_W:
					vector.addThis(0f, 0f, -1f);
					break;
				case Keyboard.KEY_RIGHT:
				case Keyboard.KEY_D:
					vector.addThis(1f, 0f, 0f);
					break;
				case Keyboard.KEY_DOWN:
				case Keyboard.KEY_S:
					vector.addThis(0f, 0f, 1f);
					break;
				case Keyboard.KEY_LEFT:
				case Keyboard.KEY_A:
					vector.addThis(-1f, 0f, 0f);
					break;
				default:
				}
			}

			vector.normalizeThis();
			vector.multThis(speed);
			model.getCameraNode().getMovement().getPosition().set(vector);

		}

		if (key == Keyboard.KEY_ESCAPE) {
			exit();
		}

	}

}
