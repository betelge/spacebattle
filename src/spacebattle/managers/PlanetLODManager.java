package spacebattle.managers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lwjgl.BufferUtils;

import spacebattle.planet.Planet;
import spacebattle.planet.QuadTree;
import spacebattle.planet.QuadTreeNode;

import lw3d.math.Vector3f;
import lw3d.renderer.CameraNode;
import lw3d.renderer.Geometry;
import lw3d.renderer.GeometryNode;
import lw3d.renderer.Material;
import lw3d.renderer.Node;
import lw3d.renderer.Geometry.Attribute;

public class PlanetLODManager {
	static CameraNode camera;
	static Set<Planet> planets = new HashSet<Planet>();
	
	static Vector3f cameraPosition;
	static Vector3f planetPosition;
	
	static Planet currentPlanet;
	
	static int size = 42;

	static IntBuffer indices = null;
	
	static public void setCamera(CameraNode camera) {
		PlanetLODManager.camera = camera;
	}
	
	static public void addPlanet(Planet planet) {
		planets.add(planet);
	}
	
	// TODO: Make private when separate thread exist.
	static public void processPlanet(Planet planet) {
		cameraPosition = camera.getAbsoluteTransform().getPosition();
		planetPosition = planet.getAbsoluteTransform().getPosition();
		currentPlanet = planet;
		
		if(indices == null) {
			indices = BufferUtils.createIntBuffer(2*size*size+4*size);
			for(int i = 0; i < size; i++) {
				indices.put((size+1)*i + size+1);			
				for(int j = 0; j < size; j++) {
					indices.put((size+1)*i+j);
					indices.put((size+1)*i+j + size+1 + 1);
				}
				indices.put((size+1)*i + size);
				
				// Degenerate triangles
				indices.put((size+1)*i + size);
				indices.put((size+1)*(i+1) + size+1);
			}
			indices.flip();
		}
		
		synchronized (planet) {
			Iterator<Node> it = planet.getChildren().iterator();
			while(it.hasNext()) {
				Node node = it.next();
				if(node instanceof QuadTreeNode) {
					processQuadTree((QuadTreeNode)node);
				}
			}
		} 
	}
	
	static private void processQuadTree(QuadTreeNode node) {
	
		boolean leafCandidate = isLeafCandidate(node);
		
		if( !leafCandidate && !node.isLeaf() ) {
			
			synchronized (node) {
				// TODO: Crazy type-casting
				Iterator<Node> it = ((Node)node).getChildren().iterator();
				while(it.hasNext()) {
					processQuadTree((QuadTreeNode)it.next());
				}
			}
		}
		else if( leafCandidate && !node.isLeaf() ) {
			
			// We need to increase detail.
			synchronized (node) {
				
				node.setLeaf(false);
				node.getChildren().clear();
			
				QuadTreeNode leaf[] = new QuadTreeNode[4];
				
				float l = node.getLength();
				
				Vector3f offsets[] = new Vector3f[4];
				Vector3f center = node.getCenter();
				Vector3f basis[] = node.getBasis();
				
				offsets[0] = center.add(basis[0].x + basis[1].x, basis[0].y + basis[1].y, basis[0].z + basis[1].z);
				offsets[1] = center.add(basis[0].x - basis[1].x, basis[0].y - basis[1].y, basis[0].z - basis[1].z);
				offsets[2] = center.add(-basis[0].x + basis[1].x, -basis[0].y + basis[1].y, -basis[0].z + basis[1].z);
				offsets[3] = center.add(-basis[0].x - basis[1].x, -basis[0].y - basis[1].y, -basis[0].z - basis[1].z);					
				
				
				leaf[0] = new QuadTreeNode(offsets[0], l/2, true);
				leaf[1] = new QuadTreeNode(offsets[1], l/2, true);
				leaf[2] = new QuadTreeNode(offsets[2], l/2, true);
				leaf[3] = new QuadTreeNode(offsets[3], l/2, true);

				for(int i = 0; i < 4; i++) {
					
					Geometry geometry = geometryGenerator(leaf[i]);
					
					leaf[i].attach(new GeometryNode( geometry, Material.DEFAULT ));
					
					node.attach(leaf[i]);
				}
				
			}
			
			//Set<Node>  siblings = ((Node)node).getParent().getChildren();
			
			/*synchronized (siblings) {
				((Node)node).getParent().attach((Node)node);
				((Node)node).detachFromParent();
			}*/
			
		} else if( !leafCandidate && node.isLeaf() ){
			
			// We need to decrease detail.
			//QuadTreeLeafNode leaf = new QuadTreeLeafNode(null, null);
			
			//Set<Node>  siblings = ((Node)node).getParent().getChildren();
			
			/*synchronized (siblings) {
				((Node)node).getParent().attach(leaf);
				((Node)node).detachFromParent();
			}*/
			
			// TODO: Do some destruction on (Node)node.
			
		}
		
		// Node is and should be a leaf.
		// Make sure it has a geometry.
		
		if(node.getChildren().isEmpty()) {
			synchronized (node) {
				node.attach(new GeometryNode(geometryGenerator(node), Material.DEFAULT));
			}
		}
		
		return;
	}

	static private boolean isLeafCandidate(QuadTree node) {
		// TODO: calculate
		
		return false;
	}
	
	static private Geometry geometryGenerator(QuadTreeNode leaf) {
		
		Geometry.Attribute at = new Geometry.Attribute();
		at.name = "position";
		at.size = 3;
		at.type = Geometry.Type.FLOAT;
		at.buffer = BufferUtils.createFloatBuffer(3 * (size+1)*(size+1));
				
		Vector3f center = leaf.getCenter();
		Vector3f basis[] = leaf.getBasis();
		float length = leaf.getLength();
		
		Vector3f vertex = new Vector3f();
		
		for(int i = 0; i <= size; i++) {
			for(int j = 0; j <= size; j++) {
				vertex.set(center);
				vertex.addMultThis(basis[0], 2*j/size -1);
				vertex.addMultThis(basis[1], 2*i/size -1);
				generateVertex(vertex, length/size);
				
				((FloatBuffer)at.buffer).put(vertex.x);
				((FloatBuffer)at.buffer).put(vertex.y);
				((FloatBuffer)at.buffer).put(vertex.z);
				System.out.println("Putting vertex: " + vertex);
			}
		}
		at.buffer.flip();
		
		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.add(at);
		
		return new Geometry(Geometry.PrimitiveType.LINES, indices, attributes);
	}
	
	static private void generateVertex(Vector3f vertex, float resolution) {
		vertex.normalizeThis();
		//vertex.multThis((float) currentPlanet.getTerrain().getValue(vertex.x, vertex.y, vertex.z, resolution) );
	}
}
