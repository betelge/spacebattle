package spacebattle;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


import org.lwjgl.input.Keyboard;

import spacebattle.Model.GameState;
import spacebattle.managers.PlanetLODManager;
import spacebattle.nodes.PhysicalGeometryNode;
import spacebattle.nodes.PhysicalNode;
import spacebattle.nodes.Ship;
import spacebattle.planet.Planet;
import spacebattle.world.GalaxyGenerator;

import lw3d.Lw3dController;
import lw3d.Lw3dModel.RendererMode;
import lw3d.math.Matrix4;
import lw3d.math.Noise;
import lw3d.math.Quaternion;
import lw3d.math.Transform;
import lw3d.math.Vector3f;
import lw3d.renderer.CameraNode;
import lw3d.renderer.FBO;
import lw3d.renderer.FBOAttachable;
import lw3d.renderer.Geometry;
import lw3d.renderer.GeometryNode;
import lw3d.renderer.Light;
import lw3d.renderer.Material;
import lw3d.renderer.MovableNode;
import lw3d.renderer.Node;
import lw3d.renderer.RenderBuffer;
import lw3d.renderer.Renderer;
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
import lw3d.renderer.passes.ClearPass;
import lw3d.renderer.passes.SceneRenderPass;
import lw3d.renderer.passes.SetPass;
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
		
		simulator.setSimulation(new Simulation(50));
		
		// TODO: Load a menu and respond to it
		//model.setGameState(GameState.MENU);
		
		setUpLevel();
		model.setGameState(GameState.PLAY);
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
			synchronized (model.getRenderPasses()) {
				model.getCameraNode().getTransform().getRotation().multThis(rot);
			}
		}
	}

	@Override
	protected void onMouseWheel(int dWheel, int x, int y) {
		switch (model.getGameState()) {
		case PLAY:
			Quaternion rot = new Quaternion().fromAngleNormalAxis(
					dWheel * 0.001f, Vector3f.UNIT_Z);
			synchronized (model.getRenderPasses()) {
				model.getCameraNode().getTransform().getRotation().multThis(rot);
			}
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

		Geometry sphereMesh = GeometryLoader.loadObj("/sphere.obj");

		Set<Shader> shaders = new HashSet<Shader>();
		Set<Shader> directShaders = new HashSet<Shader>();
		Set<Shader> lightShaders = new HashSet<Shader>();
		Set<Shader> ellipseShaders = new HashSet<Shader>();
		Set<Shader> galaxyShaders = new HashSet<Shader>();

		try {
			shaders.add(new Shader(Shader.Type.VERTEX, StringLoader
					.loadString("/default.vertex")));
			shaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
					.loadString("/default.fragment"), StringLoader
					.loadString("/default_ff.fragment")));

			directShaders.add(new Shader(Shader.Type.VERTEX, StringLoader
					.loadString("/direct.vertex")));
			directShaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
					.loadString("/direct.fragment")));
			
			lightShaders.add(new Shader(Shader.Type.VERTEX, StringLoader
					.loadString("/default.vertex")));
			lightShaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
					.loadString("/light.fragment")));
			
			ellipseShaders.add(new Shader(Shader.Type.VERTEX, StringLoader
					.loadString("/default.vertex")));
			ellipseShaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
					.loadString("/ellipse.fragment"), StringLoader
					.loadString("/ellipse_ff.fragment")));
			
			galaxyShaders.add(new Shader(Shader.Type.VERTEX, StringLoader
					.loadString("/galaxy.vertex")));
			galaxyShaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
						.loadString("/galaxy.fragment"), StringLoader
						.loadString("/galaxy_ff.fragment")));

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ShaderProgram shaderProgram = new ShaderProgram(shaders);
		ShaderProgram directShaderProgram = new ShaderProgram(directShaders);
		ShaderProgram lightProgram = new ShaderProgram(lightShaders);
		ShaderProgram ellipseProgram = new ShaderProgram(ellipseShaders);
		
		Material defaultMaterial = new Material(shaderProgram);
		Material fboMaterial = new Material(directShaderProgram);
		Material lightMaterial = new Material(lightProgram);
		Material ellipseMaterial = new Material(ellipseProgram);

		Node rootNode = new Node();
		GeometryNode[] cubes = new GeometryNode[2 - 1];

		for (int i = 0; i < cubes.length; i++) {
			cubes[i] = new GeometryNode(sphereMesh, defaultMaterial);
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
		Texture fboTexture = new Texture(null, TextureType.TEXTURE_2D, model.getDrawWidth(),
				model.getDrawHeight(), TexelType.UBYTE, Format.GL_RGBA8, Filter.LINEAR_MIPMAP_NEAREST,
				WrapMode.CLAMP);

		RenderBuffer depthBuffer = new RenderBuffer(Format.GL_DEPTH_COMPONENT,
				model.getDrawWidth(), model.getDrawHeight());

		FBO myFBO = new FBO(fboTexture, depthBuffer, model.getDrawWidth(),
				model.getDrawHeight());
		
		
		defaultMaterial.addTexture("texture0", texture);
		fboMaterial.addTexture("source", fboTexture);
		model.getSimulatedNodes().add(rootNode);
		CameraNode cameraNode = new CameraNode(45f, (float) model.getDrawWidth() / model.getDrawHeight(),
				0.01f, 1000f);
		cameraNode.getTransform().getPosition().set(0f, 30f, -15f);
		cameraNode.getTransform().getRotation().fromAngleNormalAxis((float) -Math.PI/2.3f, Vector3f.UNIT_X);
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

		PhysicalGeometryNode cube = new PhysicalGeometryNode(sphereMesh,
				defaultMaterial);
		cube.setMass(1);
		cube.setGravitySource(true);
		
		rootNode.attach(cube);

		Light lightNode = new Light(new Transform(new Vector3f(3f, 3f, -15f),
				new Quaternion()));
		rootNode.attach(lightNode);
		GeometryNode lightSphere = new GeometryNode(sphereMesh, lightMaterial);
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
		ship.getTransform().getScale().multThis(0.1f);
		rootNode.attach(ship);
		rootNode.attach(model.getCameraNode());
		model.getCameraNode().getTransform().getPosition().z = -1f;
		GeometryNode shipGeometryNode = new GeometryNode(
				GeometryLoader.loadObj("/ship1.obj"), defaultMaterial);
		ship.attach(shipGeometryNode);
		ship.getMovement().getPosition().x = -0.0125f;
				
		GeometryNode map = new GeometryNode(Geometry.QUAD, ellipseMaterial);
		ellipseMaterial.addUniform(new Uniform("focus", 0.7f, 0.3f));
		ellipseMaterial.addUniform(new Uniform("major", 0.4f));
		cameraNode.attach(map);
		map.getTransform().setPosition(new Vector3f(0f, -0.60f,-2f));
		map.getTransform().getScale().multThis(0.1f);
		((Simulation) simulator.getSimulation()).setPlanet(cube);
		((Simulation) simulator.getSimulation()).setSatelite(ship);
		((Simulation) simulator.getSimulation()).setEclipseMaterial(ellipseMaterial);
		
		// Noise
		Texture noiseTexture = TextureLoader.generateNoiseTexture(TextureType.TEXTURE_3D, 8, 896364244502240606l);
		Set<Shader> noiseShaders = new HashSet<Shader>();
		
		try {
			noiseShaders.add(new Shader(Shader.Type.VERTEX, StringLoader
					.loadString("/planet.vertex")));
			noiseShaders.add(new Shader(Shader.Type.FRAGMENT, StringLoader
					.loadString("/noise.fragment"), StringLoader
					.loadString("/default_ff.fragment")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Material noiseMaterial = new Material(new ShaderProgram(noiseShaders));
		noiseMaterial.addTexture("noise", noiseTexture);
		
		//shipGeometryNode.setMaterial(noiseMaterial);
		cube.setMaterial(noiseMaterial);
		
		// Shadow map
		int shadowSize = 512;
		RenderBuffer depthRenderBuffer = new RenderBuffer(Format.GL_DEPTH_COMPONENT24, shadowSize, shadowSize);
		Texture depthTexture = new Texture(null, TextureType.TEXTURE_2D, shadowSize, shadowSize,
				TexelType.UBYTE, Format.GL_RGB8, Filter.NEAREST, WrapMode.CLAMP_TO_BORDER);
		depthTexture.setMipmapLevel(1f);
		FBO shadowmapFBO = new FBO(depthTexture, depthRenderBuffer, shadowSize, shadowSize);
		CameraNode lightCam = new CameraNode(45f, 1f, 1f, 100f);
		
		// Big planet
		Planet bigPlanet= new Planet();
		Material noiseShadowMaterial = new Material(
				new ShaderProgram(Shader.DEFAULT_VERTEX, new Shader(Shader.Type.FRAGMENT,
						StringLoader.loadStringExceptionless("/noise.fragment"))));
		noiseShadowMaterial.addTexture("noise", noiseTexture);
		noiseShadowMaterial.addTexture("shadow", depthTexture);
		bigPlanet.setMaterial(noiseShadowMaterial);
		PlanetLODManager.setCamera(cameraNode);
		PlanetLODManager.addPlanet(bigPlanet);
		PlanetLODManager.processPlanet(bigPlanet);
	//	PlanetLODManager.processPlanet(bigPlanet);
	//	PlanetLODManager.processPlanet(bigPlanet);
	//	bigPlanet.getTransform().setPosition(new Vector3f(0f, 0f, -800));
		bigPlanet.getTransform().getRotation().fromAngleNormalAxis((float)Math.PI/4, Vector3f.UNIT_X);
		bigPlanet.getTransform().getScale().multThis(4/*400f*/);
		MovableNode bigPlanetMover = new MovableNode();
		bigPlanetMover.getMovement().getRotation().fromAngleNormalAxis(0.002f,
				Vector3f.UNIT_Y);
		bigPlanetMover.attach(bigPlanet);
		rootNode.attach(bigPlanetMover);
		
		// Star map
		Geometry galaxy = GalaxyGenerator.generateGalaxyPointGeometry(6425745746742674257l);
		ShaderProgram galaxyProgram = new ShaderProgram(galaxyShaders);
		Material galaxyMaterial = new Material(galaxyProgram);
		GeometryNode galaxyNode = new GeometryNode(galaxy, galaxyMaterial);
		galaxyNode.getTransform().setPosition(cameraNode.getTransform().getPosition());
		galaxyNode.getTransform().getScale().multThis(1000);
		
		// FBO for renderpasses (bloom)
		FBO firstTarget = myFBO;
		
		lightNode.attach(lightCam);
		Material depthMaterial = new Material(
				new ShaderProgram(new Shader(Shader.Type.VERTEX,
						StringLoader.loadStringExceptionless("/perspective_shadow_map.vertex"))
						, new Shader(Shader.Type.FRAGMENT,
						StringLoader.loadStringExceptionless("/depth_to_rgb.fragment"))));
		model.lightMapCam = lightCam;
		model.lightMapObject = bigPlanet;
		model.lightUniform = new Uniform("shadowMatrix", false, new float[16]);
		noiseShadowMaterial.addUniform(model.lightUniform);
		model.extraPerspectiveUniform = new Uniform("extraPerspectiveMatrix", false, new float[16]);
		depthMaterial.addUniform(model.extraPerspectiveUniform);
		
		// Logo quad
		ShaderProgram ambientProgram = new ShaderProgram(Shader.DEFAULT_VERTEX,
				new Shader(Shader.Type.FRAGMENT, StringLoader.loadStringExceptionless("/ambient.fragment")));
		Material logoMaterial = new Material(ambientProgram);
		logoMaterial.addTexture("source", depthTexture/*TextureLoader.loadTextureExceptionless("/lw3d.png")*/);
		GeometryNode logo = new GeometryNode(Geometry.QUAD, logoMaterial);
		logo.getTransform().getPosition().x += 30;
		logo.getTransform().getPosition().z -= 10;
		//logo.getTransform().getRotation().fromAngleNormalAxis((float) Math.PI, Vector3f.UNIT_Y);
		logo.getTransform().getScale().multThis(15);
		rootNode.attach(logo);
		
		Runnable beforeFrameRunnable = new Runnable() {
			
			@Override
			public void run() {
				// Orient the camera for the shadow map
				model.lightMapCam.getTransform().getRotation().lookAt(
						model.lightMapObject.getAbsoluteTransform().getPosition().sub(
								model.lightMapCam.getAbsoluteTransform().getPosition()), Vector3f.UNIT_Y);
				
				Transform lightToObject =  model.lightMapCam.getAbsoluteTransform().getCameraTransform()
					.mult(model.lightMapObject.getAbsoluteTransform());
				
				Matrix4 lightMatrix =
					new Matrix4(lightToObject.toMatrix4());
				
				float[] lightPerspectiveFloats = Renderer.getPerspectiveMatrix(model.lightMapCam.getAspect(),
						model.lightMapCam.getFov(), model.lightMapCam.getzNear(), model.lightMapCam.getzFar());
				
				Matrix4 lightPerspectiveMatrix = new Matrix4(lightPerspectiveFloats);
				
				model.lightUniform.set(false, lightMatrix.mult(lightPerspectiveMatrix).getFloats());
				
				float[] f = model.getCameraNode().getAbsoluteTransform().getCameraTransform().toMatrix4();
				
				//System.out.println("f:");
				for(int i = 0; i < f.length; i++)
				//	System.out.println(f[i]);
				
				model.extraPerspectiveUniform.set(false, f);
			}
		};
		
		view.setBeforeFrameRunnable(beforeFrameRunnable);
		
		// Create render passes
		synchronized (model.getRenderPasses()) {
			// Enable depth writing
			model.getRenderPasses().add(new SetPass(SetPass.State.DEPTH_WRITE, true));
			
			// Clear the shadow map FBO
			model.getRenderPasses().add(
					new ClearPass(ClearPass.COLOR_BUFFER_BIT | ClearPass.DEPTH_BUFFER_BIT,
							shadowmapFBO));
			
			// Shadow map
			model.getRenderPasses().add(
					new SceneRenderPass(rootNode, lightCam, shadowmapFBO, depthMaterial));
			
			// Clear the FBO
			model.getRenderPasses().add(
					new ClearPass(ClearPass.COLOR_BUFFER_BIT | ClearPass.DEPTH_BUFFER_BIT,
							firstTarget));
			
			// Disable depth writing
			model.getRenderPasses().add(new SetPass(SetPass.State.DEPTH_WRITE, false));
			
			// Render the stars
			model.getRenderPasses().add(
					new SceneRenderPass(galaxyNode, cameraNode, firstTarget));
			
			// Enable depth writing
			model.getRenderPasses().add(new SetPass(SetPass.State.DEPTH_WRITE, true));
			
			// Render the scene to the FBO
			model.getRenderPasses().add(
					new SceneRenderPass(rootNode, cameraNode, firstTarget));
			
			// Disable depth writing
			model.getRenderPasses().add(new SetPass(SetPass.State.DEPTH_WRITE, false));
			
			// Apply Bloom to the FBO and put result on screen.
			model.getRenderPasses().add(
					new BloomPass(fboMaterial.getTextures().get("source"),
							model.getDrawWidth(), model.getDrawHeight()));
			
		}
	}

}
