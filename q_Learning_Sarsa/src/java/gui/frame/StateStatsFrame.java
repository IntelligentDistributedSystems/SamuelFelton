package gui.frame;

import java.awt.Dimension;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;

import mdp.GridState;
import mdp.MDPModel.AgAction;

public class StateStatsFrame extends JFrame implements Observer {
	
	Object[][] data = new Object[5][3];
	JTable table;
	public StateStatsFrame(GridState gs) {
		Object[] colNames = {"Action","Q-Value","Number of trials"};
		gs.addObserver(this);
		System.out.println("GS" + gs.getPosition());
		String title = "Stats: pos("+gs.getPosition().y + "," + gs.getPosition().x+",";
		title += gs.isTerminalState() ? "t" : "n";
		title += ")";
		setTitle(title);
		table = new JTable(data,colNames);
		JScrollPane pane = new JScrollPane(table);
		setContentPane(pane);
		pack();
		table.setRowHeight(50);
		pane.setSize(new Dimension(500,400));
		this.setSize(new Dimension(500, 400));
		setVisible(true);
		int i = 0;
		for (AgAction a : AgAction.values()) {
			data[i][0] = a.toString();
			++i;
		}
		updateData(gs);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		GridState gs = (GridState) arg0;
		updateData(gs);
	}
	public void updateData(GridState gs) {
		Map<AgAction,Double> qVals = gs.getqValues();
		Map<AgAction,Integer> trials = gs.getTrialNumbers();
		
		int i = 0;
		for (AgAction a : AgAction.values()) {
			Double q = qVals.get(a);
			Integer t = trials.get(a);
			data[i][1] = q == null ? 0 : q;
			data[i][2] = t == null ? 0 : t;
			++i;
		}
		((AbstractTableModel) table.getModel()).fireTableDataChanged();
		
	}

}
