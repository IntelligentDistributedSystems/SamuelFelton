package gui.panel;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import gui.component.Cell;
import mdp.Coordinates;
import mdp.MDPModel;

public class GridPanel extends JPanel {

	private static final long serialVersionUID = -4227848911169130060L;
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
	
	
	public Cell getCell(int i, int j) {
		return cells[i][j];
	}
	public void addActionListenerOnCells(ActionListener a) {
		for (final Cell[] row : cells) {
			for (final Cell c : row) {
				c.addActionListener(a);
			}
		}
	}
	public void addMouseListenerOnCells(MouseListener m) {
		for (final Cell[] row : cells) {
			for (final Cell c : row) {
				c.addMouseListener(m);
			}
		}
	}
	
}
