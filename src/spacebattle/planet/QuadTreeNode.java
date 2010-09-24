package spacebattle.planet;

import lw3d.math.Vector3f;
import lw3d.renderer.Node;

public class QuadTreeNode extends Node implements QuadTree {
	
	private boolean leaf; 
	private int index;
	private final Vector3f center;
	private final float length;
	private final Vector3f basis[] = new Vector3f[2];
	
	public QuadTreeNode(Vector3f center, float length, boolean leaf) {
		this.center = center;
		this.length = length;
		this.leaf = leaf;
		
		Vector3f absCenter = new Vector3f(
				(float)Math.abs(center.x), (float)Math.abs(center.y), (float)Math.abs(center.z));
		
		if(absCenter.x > absCenter.y && absCenter.x > absCenter.z) {
			basis[0] = Vector3f.UNIT_Y.mult(length);
			basis[1] = Vector3f.UNIT_Z.mult(length);
			if(center.x < 0) {
				Vector3f temp = basis[0];
				basis[0] = basis[1];
				basis[1] = temp;
			}
		}
		else if(absCenter.y > absCenter.x && absCenter.y > absCenter.z) {
			basis[0] = Vector3f.UNIT_Z.mult(length);
			basis[1] = Vector3f.UNIT_X.mult(length);
			if(center.y < 0) {
				Vector3f temp = basis[0];
				basis[0] = basis[1];
				basis[1] = temp;
			}
		}
		else {
			basis[0] = Vector3f.UNIT_X.mult(length);
			basis[1] = Vector3f.UNIT_Y.mult(length);
			if(center.z < 0) {
				Vector3f temp = basis[0];
				basis[0] = basis[1];
				basis[1] = temp;
			}
		}
	}

	public boolean isLeaf() {
		return leaf;
	}
	
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Vector3f getCenter() {
		return center;
	}

	public float getLength() {
		return length;
	}

	public Vector3f[] getBasis() {
		return basis;
	}
	
}
