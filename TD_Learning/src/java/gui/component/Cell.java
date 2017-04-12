package gui.component;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;

import complexEnv.GridState;

public class Cell extends JButton implements Observer{

	public static final Color DEFAULTCOLOR = new Color(240, 240, 240);
	public static final Color OBSTACLECOLOR = new Color(200,200,200);
	public static final Color TERMINALPOSITIVECOLOR = new Color(100,255,100);
	public static final Color TERMINALNEGATIVECOLOR = new Color(255,100,100);
	public static final Color HIGHLIGHTCOLOR = new Color(102,242,216);
	private static final String RIGHTARROW = "→";
	private static final String LEFTARROW = "←";
	private static final String DOWNARROW = "↓";
	private static final String UPARROW = "↑";
	
	private boolean isHighlighted = false;
	
	
	private GridState state;

	public Cell(GridState s) {
		state = s;
		state.addObserver(this);
		update(state,null);
		
	}
	
	

	public void update(Observable o, Object arg) {
		if (state.isObstacle()) {
			setBackground(OBSTACLECOLOR);
			setText(null);
		}
		else {
			String s = "";
			if (state.isPositiveTerminalState()) {
				setBackground(TERMINALPOSITIVECOLOR);
			}
			else if (state.isNegativeTerminalState()) {
				setBackground(TERMINALNEGATIVECOLOR);
			}
			else {
				setBackground(DEFAULTCOLOR);
				switch (state.getAction()) {
				case LEFT:
					s = LEFTARROW;
					break;
				case RIGHT:
					s = RIGHTARROW;
					break;
				case UP:
					s = UPARROW; 
					break;
				case DOWN:
					s = DOWNARROW;
					break;
				case NULL:
					s = "";
					break;
					
				}
				
			}
			
			setText("<html>"+ s + "<br/>R: " + state.getReward() + "<br/>U: " + round(state.getUtility(),4) + "<br/>T:" + state.getTrialNumber() + "</html>");
		}
	
	}
	public void updateColor() {
		Color c = null;
		if (isHighlighted) {
			c = HIGHLIGHTCOLOR;
		}
		else if (state.isObstacle()) {
			c = OBSTACLECOLOR;
		}
		else if (state.isPositiveTerminalState()) {
			c = TERMINALPOSITIVECOLOR;
		}
		else if (state.isNegativeTerminalState()) {
			c = TERMINALNEGATIVECOLOR;
		}
		else {
			c = DEFAULTCOLOR;
		}
		setBackground(c);
	}
	public GridState getState() {
		return state;
	}

	public boolean isHighlighted() {
		return isHighlighted;
	}

	
	public void setHighlighted(boolean isHighlighted) {
		this.isHighlighted = isHighlighted;
		updateColor();
	}
	public static double round(double d, int decimalPlace) {
	    BigDecimal bd = new BigDecimal(d);
	    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
	    return bd.doubleValue();
	}

	

	

}
