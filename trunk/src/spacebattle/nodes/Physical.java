package spacebattle.nodes;

import lw3d.math.Vector3f;
import lw3d.renderer.Movable;

public interface Physical extends Movable {
	
	public Vector3f getAcceleration();
	public void setAcceleration(Vector3f acceleration);
	
	public float getMass();
	public void setMass(float mass);
	
	public float getTotalMass();
	public void setTotalMass(float totalMass);
	
	public boolean isGravitySource();
	public void setGravitySource(boolean isGravitySource);
}
