package world;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;

import lw3d.renderer.Geometry;
import lw3d.renderer.Geometry.Attribute;
import lw3d.renderer.Geometry.PrimitiveType;
import lw3d.renderer.Geometry.Type;

public class GalaxyGenerator {
	public static Geometry generateGalaxyPointGeometry(long seed) {
		
		double[] noiseSeedR = new double[16*16*16];
		double[] noiseSeedI = new double[16*16*16];
		
		Random rand = new Random(seed);
		
		for(int i = 0; i < noiseSeedR.length; i++) {
			noiseSeedR[i] = 2.0 * rand.nextDouble() - 1.0;
			noiseSeedI[i] = 2.0 * rand.nextDouble() - 1.0;
		}
		
		List<Float> starPositions = new ArrayList<Float>();
		
		// Go through all sectors
		for(int x = 0; x < 16; x++) {
			for(int y = 0; y < 16; y++) {
				for(int z = 0; z < 16; z++) {
					
					// Amount of stars in this sector
					int amount = (int) ( 100.0 * Math.sin(x
							+ noise(noiseSeedR, noiseSeedI, x, y, z) 
							+ 0.5*noise(noiseSeedR, noiseSeedI, 2*x, 2*y, 2*z) 
							+ 0.25*noise(noiseSeedR, noiseSeedI, 4*x, 4*y, 4*z)
							+ 0.125*noise(noiseSeedR, noiseSeedI, 8*x, 8*y, 8*z)) );
					for(int i = 0; i < amount; i++) {
						// Put a star in the sector
						starPositions.add(((float)x + rand.nextFloat())/16f - 0.5f);
						starPositions.add(((float)y + rand.nextFloat())/16f - 0.5f);
						starPositions.add(((float)z + rand.nextFloat())/16f - 0.5f);
					}	
				}
			}
		}
		
		List<Attribute> attributes = new ArrayList<Attribute>();
		Attribute at = new Attribute();
		at.buffer = BufferUtils.createFloatBuffer(starPositions.size());
		at.name = "position";
		at.size = 3;
		at.type = Type.FLOAT;

		Iterator<Float> fIt = starPositions.iterator();
		while (fIt.hasNext())
			((FloatBuffer) at.buffer).put(fIt.next());
		at.buffer.flip();

		attributes.add(at);
		
		// TODO: Are the indices needed?
		int amountOfStars = starPositions.size() / 3;
		IntBuffer indices = BufferUtils.createIntBuffer(amountOfStars);
		for(int i = 0; i < amountOfStars; i++) {
			indices.put(i);
		}
		indices.flip();
		
		return new Geometry(PrimitiveType.POINTS, indices, attributes);
	}
	
	private static double noise(double[] noiseSeedR, double[] noiseSeedI, int x, int y, int z) {
		// Modulate the coordinate into the sectors
		x %= 256;
		y %= 256;
		z %= 256;
		
		int indexH = (x % 16) + 16 * (y % 16) + 256 * (z % 16);
		int indexL = (int) (Math.floor((double) x / 16) + 16 * Math.floor((double) y / 16) + 256 * Math.floor((double) y / 16));
		double phase = Math.PI * noiseSeedR[indexL];
		return noiseSeedR[indexH] * Math.cos(phase) + noiseSeedI[indexL] * Math.sin(phase);
	}
}
