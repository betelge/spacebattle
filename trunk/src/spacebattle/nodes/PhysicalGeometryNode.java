package spacebattle.nodes;

import lw3d.math.Vector3f;
import lw3d.renderer.Geometry;
import lw3d.renderer.Material;
import lw3d.renderer.MovableGeometryNode;

public class PhysicalGeometryNode extends MovableGeometryNode implements Physical {

	public PhysicalGeometryNode(Geometry geometry, Material material) {
		super(geometry, material);
	}

	private float mass;
	private float totalMass;
	public float getTotalMass() {
		return totalMass;
	}

	public void setTotalMass(float totalMass) {
		this.totalMass = totalMass;
	}

	private Vector3f acceleration = new Vector3f();
	private boolean isGravitySource = false;

	@Override
	public Vector3f getAcceleration() {
		return acceleration;
	}

	@Override
	public float getMass() {
		return mass;
	}

	@Override
	public boolean isGravitySource() {
		return isGravitySource;
	}

	@Override
	public void setAcceleration(Vector3f acceleration) {
		this.acceleration = acceleration;
	}

	@Override
	public void setGravitySource(boolean isGravitySource) {
		this.isGravitySource = isGravitySource;
	}

	@Override
	public void setMass(float mass) {
		this.mass = mass;
	}

}
