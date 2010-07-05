package spacebattle;
import java.awt.Canvas;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.opengl.Display;

import lw3d.Lw3dModel;
import lw3d.math.Quaternion;
import lw3d.math.Transform;
import lw3d.math.Vector3f;
import lw3d.renderer.CameraNode;
import lw3d.renderer.FBO;
import lw3d.renderer.Geometry;
import lw3d.renderer.GeometryNode;
import lw3d.renderer.Light;
import lw3d.renderer.Material;
import lw3d.renderer.MovableGeometryNode;
import lw3d.renderer.Node;
import lw3d.renderer.RenderBuffer;
import lw3d.renderer.ShaderProgram;
import lw3d.renderer.Texture;
import lw3d.renderer.Uniform;
import lw3d.renderer.FBOAttachable.Format;
import lw3d.renderer.ShaderProgram.Shader;
import lw3d.renderer.Texture.Filter;
import lw3d.renderer.Texture.TexelType;
import lw3d.renderer.Texture.TextureType;
import lw3d.renderer.Texture.WrapMode;
import lw3d.renderer.passes.SceneRenderPass;
import lw3d.utils.GeometryLoader;
import lw3d.utils.StringLoader;
import lw3d.utils.TextureLoader;


public class Model extends Lw3dModel {

	public Model() {
		this(null);
	}

	public Model(Canvas displayParent) {
		super(displayParent);

		InputStream is = getClass().getResourceAsStream("/untitled.obj");
		if(is == null)
			System.out.println("Model cant't load geometry: " + "/untitled.obj");
		else {
			System.out.println("Model loads ok");
		}

		Geometry cubeMesh = GeometryLoader.loadObj("/untitled.obj");

		Set<Shader> shaders = new HashSet<Shader>();
		Set<Shader> fboShaders = new HashSet<Shader>();
		
		try {
			shaders
					.add(new Shader(
							Shader.Type.VERTEX,
							StringLoader.loadString("/default.vertex")));

			shaders
					.add(new Shader(
							Shader.Type.FRAGMENT,
							StringLoader.loadString("/default.fragment")));
			
			fboShaders
				.add(new Shader(
					Shader.Type.VERTEX,
					StringLoader.loadString("/direct.vertex")));
			fboShaders
				.add(new Shader(
					Shader.Type.FRAGMENT,
					StringLoader.loadString("/direct.fragment")));

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		ShaderProgram shaderProgram = new ShaderProgram(shaders);
		ShaderProgram fboShaderProgram = new ShaderProgram(fboShaders);
		Material defaultMaterial = new Material(shaderProgram);
		Material fboMaterial = new Material(fboShaderProgram);
		
		Node rootNode = new Node();
		GeometryNode[] cubes = new GeometryNode[1-1];
		
		for(int i = 0; i < cubes.length; i++) {
			cubes[i] = new GeometryNode(cubeMesh, defaultMaterial);
			cubes[i].getTransform().getPosition().z = -100 * (float)Math.random() - 5;
			cubes[i].getTransform().getPosition().x = cubes[i].getTransform().getPosition().z * (0.5f-(float)Math.random());
			cubes[i].getTransform().getPosition().y = cubes[i].getTransform().getPosition().z * (0.5f-(float)Math.random());
			
			rootNode.attach(cubes[i]);
		}
		Uniform[] uniforms = new Uniform[1];
		uniforms[0] = new Uniform("col2", 0f, 1f, 0f, 1f);
		defaultMaterial.setUniforms(uniforms);
		
		Texture texture = null;
		try {
			texture = TextureLoader.loadTexture("/test.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// FBO texture
		Texture fboTexture = new Texture(null, TextureType.TEXTURE_2D,
				Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight(),
				TexelType.UBYTE, Format.GL_RGBA8, Filter.LINEAR, WrapMode.CLAMP);
		
		RenderBuffer depthBuffer = new RenderBuffer(Format.GL_DEPTH_COMPONENT,
				Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
		
		FBO myFBO = new FBO(fboTexture, depthBuffer,
				Display.getDisplayMode().getWidth(), Display.getDisplayMode().getHeight());
		
		defaultMaterial.addTexture("texture0", texture);
		fboMaterial.addTexture("source", fboTexture);
		simulatedNodes.add(rootNode);
		cameraNode = new CameraNode();
		rootNode.attach(cameraNode);
		//cameraNode.getTransform().getPosition().z = -1f;
		
		// Create render passes
		renderPasses.add(new SceneRenderPass(rootNode, cameraNode/*, myFBO*/));
		//renderPasses.add(new QuadRenderPass(fboMaterial));
		//renderPasses.add(new BloomPass(fboMaterial.getTextures().get("source")));
		
		MovableGeometryNode cube = new MovableGeometryNode(cubeMesh, defaultMaterial);
		
		rootNode.attach(cube);
		
		rootNode.attach(new Light(new Transform(new Vector3f(3f,3f,-15f), new Quaternion())));

		cube.getTransform().getPosition().z = -15f;
		cube.getTransform().getPosition().x = 1f;
		//cube.getTransform().getPosition().y = -1f;
		
		/*cube.getTransform().getRotation().fromAngleAxis(
				(float)Math.PI/2*3, new Vector3f(1f, 2f, 1f));*/

		cube.getMovement().getPosition().x = -0.01f;
		cube.getMovement().getRotation().fromAngleNormalAxis(0.03f, Vector3f.UNIT_Z);
	}

}
