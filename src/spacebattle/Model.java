package spacebattle;
import java.awt.Canvas;

import spacebattle.nodes.Ship;



import lw3d.Lw3dModel;
import lw3d.math.Matrix4;
import lw3d.renderer.CameraNode;
import lw3d.renderer.Node;
import lw3d.renderer.Uniform;

public class Model extends Lw3dModel {
	
	private GameState gameState = GameState.INIT;
	
	public CameraNode lightMapCam;
	public Node lightMapObject;
	public Matrix4 lightMatrix;
	public Uniform lightUniform;
	public Uniform extraPerspectiveUniform;
	
	public enum GameState {
		INIT, MENU, PLAY, PAUSE
	}
	
	private Ship ownShip; 

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

	public void setOwnShip(Ship ownShip) {
		this.ownShip = ownShip;
	}

	public Ship getOwnShip() {
		return ownShip;
	}

}
