package gui.component;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;

import mdp.GridState;
import mdp.MDPModel.AgAction;

public class Cell extends JButton implements Observer {

	private static final long serialVersionUID = -6731687395759194356L;
	public static final Color DEFAULTCOLOR = new Color(240, 240, 240);
	public static final Color OBSTACLECOLOR = new Color(200, 200, 200);
	public static final Color TERMINALPOSITIVECOLOR = new Color(100, 255, 100);
	public static final Color TERMINALNEGATIVECOLOR = new Color(255, 100, 100);
	public static final Color HIGHLIGHTCOLOR = new Color(102, 242, 216);
	public static final Color AGENTCOLOR = new Color(255, 0, 255);
	private static final String RIGHTARROW = "→";
	private static final String LEFTARROW = "←";
	private static final String DOWNARROW = "↓";
	private static final String UPARROW = "↑";
	

	private boolean isHighlighted = false;
	private boolean isAgentPlace = false;

	private GridState state;

	public Cell(GridState s) {
		state = s;
		state.addObserver(this);
		update(state, null);

	}

	private void setTextForState() {
		String s = "<html>";
		
		if (state.isObstacle() || state.isTerminalState()) {
			s += "R : " + state.getReward();
		} else {
			AgAction a = state.getBestAction();
			Double bestQValue = null;
			Integer trialNumber = null;
			if (a != null) {
				bestQValue = state.getQValue(a);
				trialNumber = state.getTrialNumberForAction(a);
				
				s += getSymbolForAction(a) + "<br/>R : " + state.getReward() + "<br/>Q : " + round(bestQValue,2) + "<br/>T : "
						+ trialNumber;
			}
			else {
				s += "R : " + state.getReward();
			}

		}
		s += "</html>";
		setText(s);
	}

	public void update(Observable o, Object arg) {
		setColorForState();
		setTextForState();
		

	}
	public static double round(double d, int decimalPlace) {
	    BigDecimal bd = new BigDecimal(d);
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	    return bd.doubleValue();
	}
	

	public void setColorForState() {
		Color c = null;
		
		if (isHighlighted) {
			c = HIGHLIGHTCOLOR;
		} else if (state.isObstacle()) {
			c = OBSTACLECOLOR;
		} else if (state.isPositiveTerminalState()) {
			c = TERMINALPOSITIVECOLOR;
		} else if (state.isNegativeTerminalState()) {
			c = TERMINALNEGATIVECOLOR;
		} else {
			c = DEFAULTCOLOR;
		}
		setBackground(c);
	}
	private String getSymbolForAction(AgAction a) {
		String s = null;
		switch (a) {
		case LEFT:
			s = LEFTARROW;
			break;
		case RIGHT:
			s = RIGHTARROW;
			break;
		case DOWN:
			s = DOWNARROW;
			break;
		case UP:
			s = UPARROW;
			break;
		case NULL:
			s = "";
			break;
		}
		
		return s;
	}

	public GridState getState() {
		return state;
	}

	public boolean isHighlighted() {
		return isHighlighted;
	}

	public void setHighlighted(boolean isHighlighted) {
		this.isHighlighted = isHighlighted;
		setColorForState();
	}

	public boolean isAgentPlace() {
		return isAgentPlace;
	}

	public void setAgentPlace(boolean isAgentPlace) {
		this.isAgentPlace = isAgentPlace;
		setColorForState();
	}
	

}
