package gui.frame;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import gui.component.Cell;
import gui.panel.GridPanel;
import mdp.Coordinates;
import mdp.GridState;
import mdp.MDPEnv;
import mdp.MDPModel;

public class MDPView extends JFrame {

	private static final long serialVersionUID = -311502483507154556L;
	GridPanel gp;
	public static final int HEIGHT = 300;
	public static final int WIDTH = 300;

	private MDPEnv mdpEnv;
	private MDPModel mdpModel;
	private GridBagLayout gblMain;
	private FormPanel fp;
	private List<Cell> selectedCells = new ArrayList<Cell>();
	private Cell agentPos = null;
	Cell selectedCell = null;

	public MDPView(MDPEnv env, MDPModel model) {
		super("MDP View");
		this.mdpEnv = env;
		this.mdpModel = model;
		fp = new FormPanel();
		gblMain = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		getContentPane().setLayout(gblMain);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 5;
		gbc.gridheight = 3;
		gbc.fill = GridBagConstraints.BOTH;
		setSize(new Dimension(WIDTH, HEIGHT));
		gp = new GridPanel(model);
		add(gp, gbc);
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 1;
		add(fp, gbc);
		initMenu();

		setVisible(true);
		MouseListener ml_cells = new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {}
			
			@Override
			public void mousePressed(MouseEvent e) {}
			
			@Override
			public void mouseExited(MouseEvent e) {}
			
			@Override
			public void mouseEntered(MouseEvent e) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				Cell c = (Cell) e.getSource();
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (selectedCells.contains(c)) {
						selectedCells.remove(c);
						c.setHighlighted(false);
					} else {
						selectedCells.add(c);
						c.setHighlighted(true);
					}
					if (selectedCells.isEmpty()) {
						fp.setFormEnabled(false);
					} else {
						fp.setFormEnabled(true);
					}
				} else {
					new StateStatsFrame(c.getState());
				}
			}
		};

		pack();
		gp.addMouseListenerOnCells(ml_cells);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	private void initMenu() {

		final FileNameExtensionFilter filtre = new FileNameExtensionFilter("MDP Environment (*.mdpenv)", "mdpenv");

		final JMenuBar jmbMain = new JMenuBar();
		final JMenu options = new JMenu("Environment");
		final JMenuItem size = new JMenuItem("Size");
		final JMenuItem emptyEnv = new JMenuItem("Empty environment");
		final JMenuItem save = new JMenuItem("Save");
		final JMenuItem load = new JMenuItem("Load");
		final JMenuItem saveQV = new JMenuItem("save Q-values");

		save.setMnemonic(KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));

		load.setMnemonic(KeyEvent.VK_L);
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));

		emptyEnv.setMnemonic(KeyEvent.VK_E);
		emptyEnv.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));

		size.setMnemonic(KeyEvent.VK_Z);
		size.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK));

		options.add(emptyEnv);
		options.add(size);
		options.add(save);
		options.add(load);
		options.add(saveQV);

		jmbMain.add(options);

		/**
		 * Opens a dialog where the size is selectable
		 */
		size.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				new SizeDialog();

			}
		});

		/**
		 * Opens a file chooser where the user can select the file in which to
		 * save the environment (then it saves it)
		 */
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser saveMenu = new JFileChooser("%userprofile%\\documents");
				saveMenu.setDialogTitle("Sauvegarder la partie");
				saveMenu.setFileFilter(filtre);
				saveMenu.setMultiSelectionEnabled(false);
				File chosenFile = null;
				int returnVal;
				boolean impossibleSave = false;
				do {
					returnVal = saveMenu.showSaveDialog(MDPView.this);
					impossibleSave = false;
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						chosenFile = saveMenu.getSelectedFile();
						if (!chosenFile.exists() && !chosenFile.getName().endsWith(".mdpenv")) {
							saveMenu.setSelectedFile(new File(chosenFile.getPath() + ".mdpenv"));
						}
						if (!filtre.accept(saveMenu.getSelectedFile())) {
							impossibleSave = true;
							JOptionPane.showMessageDialog(new JFrame(), "Incorrect Type !", "Incorrect type!",
									JOptionPane.ERROR_MESSAGE);
						} else
							impossibleSave = false;
					}
				} while (impossibleSave);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					mdpEnv.save(saveMenu.getSelectedFile());
				}
			}
		});
		/**
		 * Opens a file chooser where the user can select the environment to
		 * load
		 */
		load.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser loadMenu = new JFileChooser("%userprofile%\\documents");
				loadMenu.setDialogTitle("Load an environment");
				loadMenu.setFileFilter(filtre);
				loadMenu.setMultiSelectionEnabled(false);
				boolean impossibleLoading = false;

				int returnVal;
				do {
					returnVal = loadMenu.showOpenDialog(MDPView.this);
					if (!filtre.accept(loadMenu.getSelectedFile()) && returnVal == JFileChooser.APPROVE_OPTION) {
						impossibleLoading = true;
						JOptionPane.showMessageDialog(new JFrame(), "Incorrect file type !", "Incorrect file type !",
								JOptionPane.ERROR_MESSAGE);
					} else
						impossibleLoading = false;

				} while (impossibleLoading);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					mdpEnv.load(loadMenu.getSelectedFile());
				}
			}
		});

		/**
		 * Resets the environment
		 */
		emptyEnv.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

			}
		});
		saveQV.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				Object[] possibilities = {"ham", "spam", "yam"};
				String s = (String)JOptionPane.showInputDialog(
				                    MDPView.this,
				                    "Input the name of the simulation:\n"
				                    + "The CSV files will be simulationName_agentName.csv",
				                    "CSV export",
				                    JOptionPane.PLAIN_MESSAGE,
				                    null,
				                    null,
				                    "");
				if (s != null && !s.isEmpty()) {
					MDPView.this.mdpEnv.saveQValuesAsCSV(s);
					MDPView.this.mdpEnv.generateXLS(s);
				}
				/*//If a string was returned, say so.
				if ((s != null) && (s.length() > 0)) {
				    setLabel("Green eggs and... " + s + "!");
				    return;
				}

				//If you're here, the return value was null/empty.
				setLabel("Come on, finish the sentence!");*/
				
			}
		});
		

		setJMenuBar(jmbMain);
	}

	public Cell getAgentPos() {
		return agentPos;
	}

	public void setAgentPos(Cell agentPos) {
		if (this.agentPos != null) {
			this.agentPos.setAgentPlace(false);
		}
		this.agentPos = agentPos;
		this.agentPos.setAgentPlace(true);
	}
	public void setAgentPos(Coordinates c) {
		Cell cell = gp.getCell(c.y, c.x);
		setAgentPos(cell);
	}

	protected class FormPanel extends JPanel {

		private static final long serialVersionUID = -2146598571069924332L;
		private static final String DEFAULTTYPE = "Free space";
		private static final String POSITIVETERMINAL = "Positive terminal location";
		private static final String NEGATIVETERMINAL = "Negative terminal location";
		private static final String OBSTACLETYPE = "Obstacle";

		private JComboBox<String> types;
		private JSpinner reward;
		private JButton validate, run;

		public FormPanel() {
			Font font = new Font("Arial", Font.PLAIN, 20);

			types = new JComboBox<String>();
			types.addItem(DEFAULTTYPE);
			types.addItem(OBSTACLETYPE);
			types.addItem(POSITIVETERMINAL);
			types.addItem(NEGATIVETERMINAL);
			types.setFont(font);

			((JLabel) types.getRenderer()).setHorizontalAlignment(JLabel.CENTER);

			setLayout(new GridLayout(4, 1));
			add(types);

			reward = new JSpinner(new SpinnerNumberModel(0, -1000.0, 1000.0, 0.01));
			JComponent spinnerEditorComp = reward.getEditor();
			if (spinnerEditorComp instanceof JSpinner.DefaultEditor) {
				JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinnerEditorComp;
				editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
			}
			add(reward);
			reward.setFont(font);
			validate = new JButton("Validate");
			validate.setFont(font);
			validate.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					for (final Cell c : MDPView.this.selectedCells) {
						final GridState gs = c.getState();
						gs.setReward((Double) reward.getValue());

						final String type = (String) types.getSelectedItem();
						boolean isObstacle = false;
						boolean isTerminalPositive = false;
						boolean isTerminalNegative = false;

						if (type.equals(OBSTACLETYPE)) {
							isObstacle = true;
						} else if (type.equals(POSITIVETERMINAL)) {
							isTerminalPositive = true;
						} else if (type.equals(NEGATIVETERMINAL)) {
							isTerminalNegative = true;
						}
						gs.setObstacle(isObstacle);
						gs.setPositiveTerminalState(isTerminalPositive);
						gs.setNegativeTerminalState(isTerminalNegative);

						c.setHighlighted(false);
					}
					MDPView.this.selectedCells.clear();

					FormPanel.this.setFormEnabled(false);

				}
			});
			add(validate);

			run = new JButton("Run");
			run.setFont(font);
			run.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					MDPView.this.mdpEnv.askAgentsToRun();
					setFormEnabled(false);
					MDPView.this.selectedCells.clear();
				}
			});
			add(run);

			this.setFormEnabled(false);

		}

		public void setFormEnabled(boolean b) {
			validate.setEnabled(b);
			types.setEnabled(b);
			reward.setEnabled(b);

		}
	}

	public class SizeDialog extends JFrame {

		private static final long serialVersionUID = 4342002647515935446L;
		final JPanel jpRow = new JPanel();
		final JPanel jpCol = new JPanel();
		final Coordinates size = MDPView.this.mdpModel.getSize();
		final JSpinner rowNumber = new JSpinner(new SpinnerNumberModel(size.y, 3, 100, 1));
		final JSpinner colNumber = new JSpinner(new SpinnerNumberModel(size.x, 3, 100, 1));
		final JButton jbValidate = new JButton("Ok");

		public SizeDialog() {
			jpRow.setLayout(new GridLayout(1, 2));
			jpCol.setLayout(new GridLayout(1, 2));
			jpRow.add(new JLabel("Rows : "));
			jpRow.add(rowNumber);

			jpCol.add(new JLabel("Columns : "));
			jpCol.add(colNumber);
			jbValidate.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					int cols = (Integer) (colNumber.getValue());
					int rows = (Integer) (rowNumber.getValue());
					mdpEnv.setSize(new Coordinates(cols, rows));
					MDPView.this.dispose();
					SizeDialog.this.dispose();

				}
			});
			getContentPane().setLayout(new GridLayout(3, 1));
			add(jpRow);
			add(jpCol);
			add(jbValidate);
			pack();
			setSize(new Dimension(600, 200));
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			setVisible(true);

		}
	}

}
