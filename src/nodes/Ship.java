package nodes;

import lw3d.math.Vector3f;

public class Ship extends PhysicalNode {
	private Vector3f mainEngineForce = new Vector3f(0f, 0f, -0.0000005f);
	
	private float mainEngineValue = 0f;
	
	public Vector3f getMainEngineForce() {
		return mainEngineForce;
	}

	public void setMainEngineValue(float mainEngineValue) {
		if(mainEngineValue < 0f)
			this.mainEngineValue = 0f;
		else if(mainEngineValue > 1f)
			this.mainEngineValue = 1f;
		else
			this.mainEngineValue = mainEngineValue;
	}

	public float getMainEngineValue() {
		return mainEngineValue;
	}
}
