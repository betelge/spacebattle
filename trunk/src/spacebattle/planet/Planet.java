package spacebattle.planet;

import java.util.Iterator;

import spacebattle.procedurals.fBm;
import lw3d.math.Noise;
import lw3d.math.Procedural;
import lw3d.math.Vector3f;
import lw3d.renderer.GeometryNode;
import lw3d.renderer.Material;
import lw3d.renderer.Node;

public class Planet extends Node {
	private Procedural terrain = new fBm(7436734673467l); // TODO: remove initialization
	private Material material = Material.DEFAULT;
	
	public Planet() {
		// Attach the 6 sides of the cube. 
		
		QuadTreeNode side[] = new QuadTreeNode[6];
		
		side[0] = new QuadTreeNode(Vector3f.UNIT_X, 1f, true);
		side[1] = new QuadTreeNode(Vector3f.UNIT_Y, 1f, true);
		side[2] = new QuadTreeNode(Vector3f.UNIT_Z, 1f, true);
		
		side[3] = new QuadTreeNode(Vector3f.UNIT_X.mult(-1f), 1f, true);
		side[4] = new QuadTreeNode(Vector3f.UNIT_Y.mult(-1f), 1f, true);
		side[5] = new QuadTreeNode(Vector3f.UNIT_Z.mult(-1f), 1f, true);

		for(int i = 0; i < side.length; i++) {
			attach(side[i]);
		}
	}

	public void setTerrain(Procedural terrain) {
		this.terrain = terrain;
	}

	public Procedural getTerrain() {
		return terrain;
	}

	public void setMaterial(Material material) {
		this.material = material;
		
		Iterator<Node> it = getChildren().iterator();
		while(it.hasNext()) {
			Node node = it.next();
			if(node instanceof QuadTreeNode)
				((QuadTreeNode) node).setMaterial(material);
		}
	}

	public Material getMaterial() {
		return material;
	}
	
}
