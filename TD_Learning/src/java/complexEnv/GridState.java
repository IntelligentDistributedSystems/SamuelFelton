package complexEnv;

import java.io.Serializable;
import java.util.Observable;

import complexEnv.MDPModel.AgAction;

public class GridState extends Observable implements Serializable{


	private static final long serialVersionUID = -7785386348228865473L;

	private boolean isObstacle;
	private double reward;
	private boolean isPositiveTerminalState;
	private boolean isNegativeTerminalState;
	private AgAction action = AgAction.NULL;
	
	private double utility;
	private int trialNumber;
	
	
	public GridState(double reward,boolean isObstacle) {
		this.isObstacle = isObstacle;
		this.reward = reward;
		isPositiveTerminalState = false;
		isNegativeTerminalState = false;
	}
	
	public boolean isObstacle() {
		return isObstacle;
	}

	public void setObstacle(boolean isObstacle) {
		this.isObstacle = isObstacle;
		setChanged();
		notifyObservers();
	}

	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
		
		setChanged();
		notifyObservers();
	}
	
	public void removeTerminalState() {
		isPositiveTerminalState = false;
		isNegativeTerminalState = false;
		setChanged();
		notifyObservers();
	}
	
	public void addTerminalState(boolean isPositive) {
		if (isPositive) {
			isPositiveTerminalState = true;
			isNegativeTerminalState = false;
			
		} else {
			isNegativeTerminalState = true;
			isPositiveTerminalState = false;
			
		}
		setChanged();
		notifyObservers();
	}
	public boolean isTerminalState() {
		return isPositiveTerminalState || isNegativeTerminalState;
	}

	public boolean isPositiveTerminalState() {
		return isPositiveTerminalState;
	}

	public boolean isNegativeTerminalState() {
		return isNegativeTerminalState;
	}

	public AgAction getAction() {
		return action;
	}

	public void setAction(AgAction action) {
		this.action = action;
		setChanged();
		notifyObservers();
	}

	public double getUtility() {
		return utility;
	}

	public int getTrialNumber() {
		return trialNumber;
	}

	public void setUtility(double utility) {
		this.utility = utility;
		setChanged();
		notifyObservers();
	}

	public void setTrialNumber(int trialNumber) {
		this.trialNumber = trialNumber;
		setChanged();
		notifyObservers();
	}

	public void setPositiveTerminalState(boolean isPositiveTerminalState) {
		this.isPositiveTerminalState = isPositiveTerminalState;
	}

	public void setNegativeTerminalState(boolean isNegativeTerminalState) {
		this.isNegativeTerminalState = isNegativeTerminalState;
	}
	
	
	
	
	
}
