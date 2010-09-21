package spacebattle;
import java.awt.Canvas;

import spacebattle.nodes.Ship;



import lw3d.Lw3dModel;

public class Model extends Lw3dModel {
	
	private GameState gameState = GameState.INIT;
	
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
