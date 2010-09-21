package spacebattle.world;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;

import lw3d.math.Noise;
import lw3d.renderer.Geometry;
import lw3d.renderer.Geometry.Attribute;
import lw3d.renderer.Geometry.PrimitiveType;
import lw3d.renderer.Geometry.Type;

public class GalaxyGenerator {
	public static Geometry generateGalaxyPointGeometry(long seed) {
		
		Random rand = new Random(seed);
		
		/*double[] noiseSeedR = new double[16*16*16];
		double[] noiseSeedI = new double[16*16*16];
		
		for(int i = 0; i < noiseSeedR.length; i++) {
			noiseSeedR[i] = 2.0 * rand.nextDouble() - 1.0;
			noiseSeedI[i] = 2.0 * rand.nextDouble() - 1.0;
		}*/
		
		
		
		List<Float> starPositions = new ArrayList<Float>();
		List<Byte> color = new ArrayList<Byte>();
		
		
		//Noise noiseSeedR = new Noise(seed),
		//	noiseSeedI = new Noise(seed ^42l);
		
		int subdivs = 50;
		//int starsTotal = 100;
		
		// Go through all sectors
		for(float x = 0.5f; x < subdivs; x++) {
			for(float y = 0.5f; y < subdivs; y++) {
				for(float z = 0.5f; z < subdivs; z++) {
					
					// Amount of stars in this sector
					/*int amount = (int) ( (float)starsTotal/subdivs *Math.sin( x/Math.PI + 
							noise(noiseSeedR, noiseSeedI, x/subdivs, y/subdivs, z/subdivs) 
							+ 0.5*noise(noiseSeedR, noiseSeedI, 2*x/subdivs, 2*y/subdivs, 2*z/subdivs) 
							+ 0.25*noise(noiseSeedR, noiseSeedI, 4*x/subdivs, 4*y/subdivs, 4*z/subdivs)
							+ 0.125*noise(noiseSeedR, noiseSeedI, 8*x/subdivs, 8*y/subdivs, 8*z/subdivs)) );*/
					
					int amount = 1;
					for(int i = 0; i < amount; i++) {
						// Put a star in the sector
						starPositions.add(((float)x + rand.nextFloat())/subdivs - 0.5f);
						starPositions.add(((float)y + rand.nextFloat())/subdivs - 0.5f);
						starPositions.add(((float)z + rand.nextFloat())/subdivs - 0.5f);
						
						// TODO: Improve colors
						
						float c = rand.nextFloat();
						
						if(c < 0.1f) {
							color.add((byte)255);
							color.add((byte)(210-(c-1f)));
							color.add((byte)(170-(c-1f)));
						}
						else if( c < 0.9f) {
							color.add((byte)255);
							color.add((byte)255);
							color.add((byte)255);
						}
						else {
							color.add((byte)(190*c*c));
							color.add((byte)(210*c));
							color.add((byte)255);
						}
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
		
		Attribute atColor = new Attribute();
		atColor.buffer = BufferUtils.createByteBuffer(color.size());
		atColor.name = "color";
		atColor.normalized = true;
		atColor.size = 3;
		atColor.type = Type.UBYTE;

		Iterator<Byte> fItC = color.iterator();
		while (fItC.hasNext())
			((ByteBuffer) atColor.buffer).put(fItC.next());
		atColor.buffer.flip();

		attributes.add(atColor);
		
		// TODO: Are the indices needed?
		int amountOfStars = starPositions.size() / 3;
		IntBuffer indices = BufferUtils.createIntBuffer(amountOfStars);
		for(int i = 0; i < amountOfStars; i++) {
			indices.put(i);
		}
		indices.flip();
		
		return new Geometry(PrimitiveType.POINTS, indices, attributes);
	}
	
	private static double noise(Noise noiseSeedR, Noise noiseSeedI, float x, float y, float z) {
		// Modulate the coordinate into the sectors
		x %= 16;
		y %= 16;
		z %= 16;
		
		/*x += 0.11;
		y += 0.11;
		z += 0.11;*/
		
		

		double phase = Math.PI * noiseSeedR.noise(9*x / 8.0, 9*y / 8.0, 9*z / 8.0);
		return noiseSeedR.noise(9*x,9*y,9*z) * Math.cos(phase) + noiseSeedI.noise(9*x,9*y,9*z) * Math.sin(phase);
	}
}
