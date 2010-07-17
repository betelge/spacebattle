package spacebattle;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nodes.PhysicalGeometryNode;
import nodes.PhysicalNode;
import nodes.Ship;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import spacebattle.Model.GameState;

import lw3d.Lw3dController;
import lw3d.Lw3dModel;
import lw3d.Lw3dSimulation;
import lw3d.Lw3dView;
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
import lw3d.renderer.passes.BloomPass;
import lw3d.renderer.passes.QuadRenderPass;
import lw3d.renderer.passes.SceneRenderPass;
import lw3d.utils.GeometryLoader;
import lw3d.utils.StringLoader;
import lw3d.utils.TextureLoader;

public class Controller extends Lw3dController {

	Model model;
	View view;

	public Controller(Model model, View view) {
		super(model, view);

		this.model = model;
		this.view = view;

		assert (model.getGameState() == GameState.INIT);
		
		// TODO: Load a menu and respond to it
		//model.setGameState(GameState.MENU);
		
		setUpLevel();
		model.setGameState(GameState.PLAY);
		simulator.setSimulation(new Simulation(50));
		simulator.start();
	}

	@Override
	protected void onMouseMove(int dX, int dY, int x, int y) {
		switch (model.getGameState()) {
		case PLAY:
			Quaternion rot = new Quaternion().fromAngleNormalAxis(-dX * 0.01f,
					Vector3f.UNIT_Y);
			rot.multThis(new Quaternion().fromAngleNormalAxis(dY * 0.01f,
					Vector3f.UNIT_X));
			model.getCameraNode().getTransform().getRotation().multThis(rot);
		}
	}

	@Override
	protected void onMouseWheel(int dWheel, int x, int y) {
		switch (model.getGameState()) {
		case PLAY:
			Quaternion rot = new Quaternion().fromAngleNormalAxis(
					dWheel * 0.001f, Vector3f.UNIT_Z);
			model.getCameraNode().getTransform().getRotation().multThis(rot);
		}
	}

	@Override
	protected void onMouseButton(int button, boolean buttonState, int x, int y) {
		if (buttonState)
			System.out.println("Click: (" + x + ", " + y + ")");
	}

	@Override
	protected void onKey(int key, boolean isDown, boolean repeatEvent) {
		switch (model.getGameState()) {
		case PLAY:
			if (!repeatEvent) {
				Vector3f vector = new Vector3f();
				float speed = 0.15f;
				
				float mainEngineValue = 0f;

				Iterator<Integer> it = model.getKeys().iterator();
				while (it.hasNext()) {
					switch (it.next()) {
					case Keyboard.KEY_UP:
						vector.addThis(0f, 0f, -1f);
						break;
					case Keyboard.KEY_RIGHT:
						vector.addThis(1f, 0f, 0f);
						break;
					case Keyboard.KEY_DOWN:
						vector.addThis(0f, 0f, 1f);
						break;
					case Keyboard.KEY_LEFT:
						vector.addThis(-1f, 0f, 0f);
						break;
					case Keyboard.KEY_A:
						mainEngineValue = 1f;
						break;
					default:
					}
				}
				
				model.getOwnShip().setMainEngineValue(mainEngineValue);

				vector.normalizeThis();
				vector.multThis(speed);
				model.getCameraNode().getMovement().getPosition().set(vector);

			}
		}

		if (key == Keyboard.KEY_ESCAPE) {
			exit();
		}

	}

	private void setUpLevel() {

		Geometry cubeMesh = GeometryLoader.loadObj("/untitled.obj");

		Set<Shader> shaders = new HashSet<Shader>();
		Set<Shader> fboShaders = new HashSet<Shader>();
		Set<Shader> lightShaders = new HashSet<Shader>();

		try {
			shaders.add(new Shader(Shader.Type.VERTEX, StringLoader
					.loadString("/default.vertex")));
			if(!model.isUseFixedVertexPipeline())
				shaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
						.loadString("/default.fragment")));
			else
				shaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
						.loadString("/default_ff.fragment")));

			fboShaders.add(new Shader(Shader.Type.VERTEX, StringLoader
					.loadString("/direct.vertex")));
			fboShaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
					.loadString("/direct.fragment")));
			
			lightShaders.add(new Shader(Shader.Type.VERTEX, StringLoader
					.loadString("/default.vertex")));
			lightShaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
					.loadString("/light.fragment")));

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ShaderProgram shaderProgram = new ShaderProgram(shaders);
		ShaderProgram fboShaderProgram = new ShaderProgram(fboShaders);
		ShaderProgram lightProgram = new ShaderProgram(lightShaders);
		Material defaultMaterial = new Material(shaderProgram);
		Material fboMaterial = new Material(fboShaderProgram);
		Material lightMaterial = new Material(lightProgram);

		Node rootNode = new Node();
		GeometryNode[] cubes = new GeometryNode[2 - 1];

		for (int i = 0; i < cubes.length; i++) {
			cubes[i] = new GeometryNode(cubeMesh, defaultMaterial);
			cubes[i].getTransform().getPosition().z = -100
					* (float) Math.random() - 5;
			cubes[i].getTransform().getPosition().x = cubes[i].getTransform()
					.getPosition().z
					* (0.5f - (float) Math.random());
			cubes[i].getTransform().getPosition().y = cubes[i].getTransform()
					.getPosition().z
					* (0.5f - (float) Math.random());

			//rootNode.attach(cubes[i]);
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
		Texture fboTexture = new Texture(null, TextureType.TEXTURE_2D, Display
				.getDisplayMode().getWidth(), Display.getDisplayMode()
				.getHeight(), TexelType.UBYTE, Format.GL_RGBA8, Filter.LINEAR,
				WrapMode.CLAMP);

		RenderBuffer depthBuffer = new RenderBuffer(Format.GL_DEPTH_COMPONENT,
				Display.getDisplayMode().getWidth(), Display.getDisplayMode()
						.getHeight());

		FBO myFBO = new FBO(fboTexture, depthBuffer, Display.getDisplayMode()
				.getWidth(), Display.getDisplayMode().getHeight());

		defaultMaterial.addTexture("texture0", texture);
		fboMaterial.addTexture("source", fboTexture);
		model.getSimulatedNodes().add(rootNode);
		CameraNode cameraNode = new CameraNode();
		model.setCameraNode(cameraNode);
		PhysicalNode physicalNode = new PhysicalNode();
		physicalNode.setMass(1);
		physicalNode.getTransform().getPosition().z = -15f;
		physicalNode.getTransform().getPosition().x = 8f;
		//rootNode.attach(model.getCameraNode());
		rootNode.attach(physicalNode);
		physicalNode.attach(cubes[0]);
		physicalNode.getMovement().getPosition().y = 0.02f;
		cubes[0].getTransform().getPosition().multThis(0f);
		// cameraNode.getTransform().getPosition().z = -1f;

		PhysicalGeometryNode cube = new PhysicalGeometryNode(cubeMesh,
				defaultMaterial);
		cube.setMass(1);
		cube.setGravitySource(true);
		
		rootNode.attach(cube);

		Light lightNode = new Light(new Transform(new Vector3f(3f, 3f, -15f),
				new Quaternion()));
		rootNode.attach(lightNode);
		GeometryNode lightSphere = new GeometryNode(cubeMesh, lightMaterial);
		lightSphere.getTransform().getScale().multThis(0.1f);
		lightNode.attach(lightSphere);

		cube.getTransform().getPosition().z = -15f;
		cube.getTransform().getPosition().x = 1f;
		// cube.getTransform().getPosition().y = -1f;

		/*
		 * cube.getTransform().getRotation().fromAngleAxis( (float)Math.PI/2*3,
		 * new Vector3f(1f, 2f, 1f));
		 */

		//cube.getMovement().getPosition().x = -0.01f;
		cube.getMovement().getRotation().fromAngleNormalAxis(0.03f,
				Vector3f.UNIT_Z);
		
		Ship ship = new Ship();
		model.setOwnShip(ship);
		ship.setMass(0.01f);
		rootNode.attach(ship);
		cube.attach(model.getCameraNode());
		model.getCameraNode().getTransform().getPosition().z = -1f;
		GeometryNode shipGeometryNode = new GeometryNode(
				GeometryLoader.loadObj("/ship1.obj"), defaultMaterial);
		ship.attach(shipGeometryNode);
		
		
		
		// Create render passes
		synchronized (model.getRenderPasses()) {
			model.getRenderPasses().add(
					new SceneRenderPass(rootNode, cameraNode, myFBO));
			//model.getRenderPasses().add(new QuadRenderPass(fboMaterial));
			model.getRenderPasses().add(
					new BloomPass(fboMaterial.getTextures().get("source"),
							model.getDrawWidth()/4, model.getDrawHeight()/4));
			
		}
	}

}
