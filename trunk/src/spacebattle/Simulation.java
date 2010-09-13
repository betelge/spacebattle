package spacebattle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nodes.Physical;
import nodes.Ship;

import lw3d.Lw3dSimulation;
import lw3d.math.Transform;
import lw3d.math.Vector3f;
import lw3d.renderer.Material;
import lw3d.renderer.Movable;
import lw3d.renderer.Node;
import lw3d.renderer.Uniform;

public class Simulation extends Lw3dSimulation {
	
	final public float G = 0.01f;
	
	private Physical ellipsePlanet = null;
	private Movable ellipseSatelite = null;
	
	private Material ellipseMaterial = null;

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
		
		if(ellipsePlanet != null && ellipseSatelite != null && ellipseMaterial != null) {
			
			float K = G * ellipsePlanet.getMass();
			
			Vector3f relPos = ellipseSatelite.getTransform().getPosition().sub(
					ellipsePlanet.getTransform().getPosition());
			
			Vector3f relVel = ellipseSatelite.getMovement().getPosition().sub(
					ellipsePlanet.getMovement().getPosition());
			
			float timeStep = getTimeStep();
			
			// E is really E/m
			float E = 0.5f * relVel.getLengthSquared() - K / relPos.getLength();
			
			//System.out.println("Energy " + E);
						
			// (Angular momentum / mass)^2, |(r x v)|^2
			float h2 = relPos.cross(relVel).getLengthSquared();		
			
			// eccentricity
			float e = (float)Math.sqrt( 1 +  (2f * E * h2)/(K)/(K) );
			
			// semi major
			float a = h2/K/(1-e*e);//-E / (2f * K);
			
			//System.out.println("e " + e);
			
			float perigee = (1-e)*2*a;
			float apogee = (1+e)*2*a;
			
			float scale = 0.04f;// 0.25f / perigee;
			
			Uniform uniforms[] = ellipseMaterial.getUniforms();
			if(uniforms.length >= 2) {
				// Focus
				uniforms[0].set(0.5f + scale * e*a, 0.5f);
				// Major
				uniforms[1].set(scale * 2 * a);
				
				/*System.out.println("focus " + scale * (apogee - perigee));
				System.out.println("major " + scale*2*a);
				System.out.println("perigee " + scale*perigee);
				System.out.println("apogee " + scale*apogee);
				System.out.println("scale " + scale);*/
			}
		}
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
		
		if (node instanceof Ship) {
			Ship ship = (Ship) node;
			float value = ship.getMainEngineValue();
			if(value >= 0) {
				Vector3f acc = ship.getAcceleration();
				acc.set(ship.getMainEngineForce());
				acc.multThis(value);
			}
		}

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

	public Movable getPlanet() {
		return ellipsePlanet;
	}

	public void setPlanet(Physical planet) {
		this.ellipsePlanet = planet;
	}

	public Movable getSatelite() {
		return ellipseSatelite;
	}

	public void setSatelite(Movable satelite) {
		this.ellipseSatelite = satelite;
	}

	public void setEclipseMaterial(Material eclipseMaterial) {
		this.ellipseMaterial = eclipseMaterial;
	}

	public Material getEclipseMaterial() {
		return ellipseMaterial;
	}

}
