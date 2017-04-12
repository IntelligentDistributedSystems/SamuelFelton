package mdp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Observable;

import mdp.MDPModel.AgAction;

public class GridState extends Observable implements Serializable{


	private static final long serialVersionUID = -7785386348228865473L;

	private boolean isObstacle;
	private double reward;
	private boolean isPositiveTerminalState;
	private boolean isNegativeTerminalState;
	private Map<AgAction,Double> qValues;
	private Map<AgAction,Integer> trialNumbers;
	
	
	private Coordinates position;
	
	private AgAction action = AgAction.NULL;
	
	private double utility;
	private int trialNumber;
	
	
	public GridState(Coordinates pos,double reward,boolean isObstacle) {
		this.isObstacle = isObstacle;
		this.reward = reward;
		Objects.requireNonNull(pos);
		this.position = pos;
		qValues = new HashMap<>();
		trialNumbers = new HashMap<>();
		
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
	public int getTrialNumberForAction(AgAction a) {
		return trialNumbers.get(a);
	}

	public void setUtility(double utility) {
		this.utility = utility;
		setChanged();
		notifyObservers();
	}
	public void setTrialNumbers(AgAction a, int i) {
		trialNumbers.put(a, i);
		setChanged();
		notifyObservers();
	}
	public void setTrialNumber(int trialNumber) {
		this.trialNumber = trialNumber;
		setChanged();
		notifyObservers();
	}
	public Double getQValue(AgAction a) {
		return qValues.get(a);
	}
	public void setQValue(AgAction a, Double d) {
		qValues.put(a, d);
		setChanged();
		notifyObservers();
	}

	public void setPositiveTerminalState(boolean isPositiveTerminalState) {
		this.isPositiveTerminalState = isPositiveTerminalState;
	}

	public void setNegativeTerminalState(boolean isNegativeTerminalState) {
		this.isNegativeTerminalState = isNegativeTerminalState;
	}
	
	public AgAction getBestAction() {
		AgAction act = null;
		double v = 0.0;
		for (final Map.Entry<AgAction, Double> entry : qValues.entrySet()) {
			if (act == null || v < entry.getValue() && trialNumbers.get(entry.getKey()) != null ) {
				act = entry.getKey();
				v = entry.getValue();
			}
		}
		return act;
	}

	public Map<AgAction, Double> getqValues() {
		return qValues;
	}

	public Map<AgAction, Integer> getTrialNumbers() {
		return trialNumbers;
	}

	public void setqValues(Map<AgAction, Double> qValues) {
		this.qValues = qValues;
	}

	public void setTrialNumbers(Map<AgAction, Integer> trialNumbers) {
		this.trialNumbers = trialNumbers;
	}

	public Coordinates getPosition() {
		return position;
	}
	
	
	
}
