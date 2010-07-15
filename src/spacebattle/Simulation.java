package spacebattle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nodes.Physical;

import lw3d.Lw3dSimulation;
import lw3d.Lw3dSimulator;
import lw3d.math.Transform;
import lw3d.math.Vector3f;
import lw3d.renderer.Node;

public class Simulation extends Lw3dSimulation {
	
	final public float G = 0.01f;

	public Simulation(long timeStep) {
		super(timeStep);
	}

	class GravitySourceInfo {
		public float mass;
		public Vector3f position;
		public Physical physical;
	}

	Set<GravitySourceInfo> gravitySources = new HashSet<GravitySourceInfo>();

	@Override
	protected void beforeProcessingNodes() {
		gravitySources.clear();
	}

	@Override
	protected void preProcessNode(Node node) {
		preProcessNode(node, new Transform());
	}

	private void preProcessNode(Node node, Transform transform) {
		Transform currentTransform = transform.mult(node.getTransform());

		if (node instanceof Physical) {
			Physical physical = (Physical) node;

			if (physical.isGravitySource()) {
				GravitySourceInfo info = new GravitySourceInfo();
				info.mass = physical.getMass();
				info.position = currentTransform.getPosition();
				info.physical = physical;
				gravitySources.add(info);
			}
		}

		synchronized (node) {
			Iterator<Node> it = node.getChildren().iterator();
			while (it.hasNext()) {
				preProcessNode(it.next(), currentTransform);
			}
		}
	}

	@Override
	protected void processNode(Node node) {
		processNode(node, new Transform());
	}

	private void processNode(Node node, Transform transform) {
		
		//System.out.println("Simulator processing node: " + node);

		Transform currentTransform = transform.mult(node.getTransform());

		if (node instanceof Physical) {
			Physical physical = (Physical) node;
			Vector3f acc = new Vector3f(physical.getAcceleration());
			
			currentTransform.getRotation().mult(acc, acc);
			
			Iterator<GravitySourceInfo> it = gravitySources.iterator();
			while (it.hasNext()) {
				GravitySourceInfo info = it.next();
				
				if(info.physical == physical)
					continue;
				
				Vector3f dir = info.position.sub(currentTransform.getPosition());
				float force = G * info.mass * physical.getMass() / dir.getLengthSquared();
				dir.normalizeThis();
				dir.multThis(force);
				acc.addThis(dir);
			}
			
			currentTransform.getRotation().inverse().mult(acc, acc);
			physical.getMovement().getPosition().addThis(acc.mult(1f/physical.getMass()));
		}

		super.processNode(node);
	}

}
