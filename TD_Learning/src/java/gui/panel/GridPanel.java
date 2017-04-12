package gui.panel;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import complexEnv.Coordinates;
import complexEnv.MDPModel;
import gui.component.Cell;

public class GridPanel extends JPanel {

	private GridLayout layout;
	Cell[][] cells;
	
	public GridPanel(MDPModel model) {
		final Coordinates size = model.getSize();
		layout = new GridLayout(size.y, size.x);
		setPreferredSize(new Dimension(600,600));
		this.setLayout(layout);
		
		cells = new Cell[size.y][size.x];
		
		for (int i = 0; i < size.y; ++i) {
			for (int j = 0; j < size.x; ++j) {
				
				cells[i][j] = new Cell(model.getState(new Coordinates(j,i)));
				add(cells[i][j]);
				//layout.addLayoutComponent("cell " + i + j, cells[i][j]);
				
			}
		}
	}
	
	
	
	public void addActionListenerOnCells(ActionListener a) {
		for (Cell[] row : cells) {
			for (Cell c : row) {
				c.addActionListener(a);
			}
		}
	}
	
}
