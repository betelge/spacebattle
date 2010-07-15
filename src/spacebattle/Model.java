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
	
	private GameState gameState = GameState.INIT;
	
	public enum GameState {
		INIT, MENU, PLAY, PAUSE
	}

	public Model() {
		this(null);
	}

	public Model(Canvas displayParent) {
		super(displayParent);
	}

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	public GameState getGameState() {
		return gameState;
	}

}
