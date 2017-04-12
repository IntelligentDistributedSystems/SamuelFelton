package gui.frame;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
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
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import complexEnv.Coordinates;
import complexEnv.GridState;
import complexEnv.MDPEnv;
import complexEnv.MDPModel;
import complexEnv.MDPModel.AgAction;
import complexEnv.SaveException;
import gui.component.Cell;
import gui.panel.GridPanel;

public class MDPView extends JFrame {
	
	GridPanel gp;
	public static final int HEIGHT = 300;
	public static final int WIDTH = 300;
	
	private MDPEnv mdpEnv;
	private MDPModel mdpModel;
	private GridBagLayout gblMain;
	private FormPanel fp;
	private List<Cell> selectedCells = new ArrayList<Cell>();
	
	public MDPView(MDPEnv env,MDPModel model) {
		super("MDP View");
		this.mdpEnv = env;
		this.mdpModel = model;
		fp = new FormPanel();
		gblMain = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		getContentPane().setLayout(gblMain);
		gbc.gridx = 0; gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridwidth = 3;
		gbc.gridheight = 3;
		gbc.fill = GridBagConstraints.BOTH;
		setSize(new Dimension(WIDTH,HEIGHT));
		gp = new GridPanel(model);
		add(gp, gbc);
		gbc.gridx = GridBagConstraints.RELATIVE; gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 1;
		add(fp, gbc);
		initMenu();

		
		
		setVisible(true);
		ActionListener al_cells = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				Cell c = (Cell) e.getSource();
				if (selectedCells.contains(c)) {
					selectedCells.remove(c);
					c.setHighlighted(false);
				} else {
					selectedCells.add(c);
					c.setHighlighted(true);
				}
				if (selectedCells.isEmpty()) {
					fp.setFormEnabled(false);
				}
				else {
					fp.setFormEnabled(true);
				}
				
			}
		};
		pack();
		gp.addActionListenerOnCells(al_cells);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	private void initMenu() {
		
		final FileNameExtensionFilter filtre = new FileNameExtensionFilter("MDP Environment (*.mdpenv)","mdpenv");
		
		final JMenuBar jmbMain= new JMenuBar();
		final JMenu options = new JMenu("Environment");
		final JMenuItem size = new JMenuItem("Size");
		final JMenuItem emptyEnv = new JMenuItem("Empty environment");
		final JMenuItem save = new JMenuItem("Save");
		final JMenuItem load = new JMenuItem("Load");
		
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
		 * Opens a file chooser where the user can select the file in which to save the environment (then it saves it)
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
				do{
						returnVal = saveMenu.showSaveDialog(MDPView.this);
						impossibleSave = false;
						if (returnVal == JFileChooser.APPROVE_OPTION){
							chosenFile = saveMenu.getSelectedFile();
							System.out.println(chosenFile);
							if (!chosenFile.exists() && !chosenFile.getName().endsWith(".mdpenv"))
							{
								System.out.println("COUCOU");
								saveMenu.setSelectedFile(new File(chosenFile.getPath()+".mdpenv"));
							}
							if (!filtre.accept(saveMenu.getSelectedFile())){
								impossibleSave = true;
								JOptionPane.showMessageDialog(new JFrame(), "Incorrect Type !","Incorrect type!",
								        JOptionPane.ERROR_MESSAGE);
							}
							else
								impossibleSave = false;
						}
				}while (impossibleSave);
				if (returnVal == JFileChooser.APPROVE_OPTION){
					try {
						mdpEnv.save(saveMenu.getSelectedFile());
					} catch(SaveException se) {
						JOptionPane.showMessageDialog(MDPView.this,se.getMessage(), "Couldn't save !", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		/**
		 * Opens a file chooser where the user can select the environment to load
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
				do{
						returnVal = loadMenu.showOpenDialog(MDPView.this);
						if (!filtre.accept(loadMenu.getSelectedFile()) && returnVal == JFileChooser.APPROVE_OPTION){
							impossibleLoading = true;
							JOptionPane.showMessageDialog(new JFrame(), "Incorrect file type !", "Incorrect file type !",
							        JOptionPane.ERROR_MESSAGE);
						}
						else
							impossibleLoading = false;
							
				}while (impossibleLoading);
					
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
		
		
		setJMenuBar(jmbMain);
	}
	
	protected class FormPanel extends JPanel {
		private static final String DEFAULTTYPE = "Free space";
		private static final String POSITIVETERMINAL = "Positive terminal location";
		private static final String NEGATIVETERMINAL = "Negative terminal location";
		private static final String OBSTACLETYPE = "Obstacle";
		
		
		
		private JComboBox<String> types;
		private JSpinner reward;
		private JButton validate, run;
		private JComboBox<String> policies;
		public FormPanel() {
			Font font = new Font("Arial", Font.PLAIN, 20);
			types = new JComboBox<String>();
			types.addItem(DEFAULTTYPE);
			types.addItem(OBSTACLETYPE);
			types.addItem(POSITIVETERMINAL);
			types.addItem(NEGATIVETERMINAL);
			types.setFont(font);
			((JLabel) types.getRenderer()).setHorizontalAlignment(JLabel.CENTER);

			
			types.addItemListener(new ItemListener() {
				
				public void itemStateChanged(ItemEvent e) {
					String item = (String) e .getItem();
					if (item.equals(DEFAULTTYPE)) {
						policies.setEnabled(true);
					} else {
						policies.setSelectedIndex(4);
						policies.setEnabled(false);
					}
					
				}
			});
			policies = new JComboBox<String>();
			for (AgAction a : AgAction.values()) {
				policies.addItem(a.toString());
			}
			((JLabel)policies.getRenderer()).setHorizontalAlignment(JLabel.CENTER);
			setLayout(new GridLayout(5, 1));
			add(types);
			add(policies);
			policies.setFont(font);
			
			reward = new JSpinner(new SpinnerNumberModel(0, -1000.0, 1000.0, 0.01));
			reward.setFont(font);
			add(reward);
			JComponent spinnerEditorComp = reward.getEditor();
			if (spinnerEditorComp instanceof JSpinner.DefaultEditor) {
				JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinnerEditorComp;
				editor.getTextField().setHorizontalAlignment(JTextField.CENTER);
			}
			validate = new JButton("Validate");
			validate.setFont(font);
			validate.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					for (final Cell c : MDPView.this.selectedCells) {
						final GridState gs = c.getState();
						
						
				
						final String type = (String) types.getSelectedItem();
						boolean isObstacle = false;
						boolean isTerminalPositive = false;
						boolean isTerminalNegative = false;
						
						if (type.equals(OBSTACLETYPE)) {
							isObstacle = true;
						}
						else if (type.equals(POSITIVETERMINAL)) {
							isTerminalPositive = true;
						}
						else if (type.equals(NEGATIVETERMINAL)) {
							isTerminalNegative = true;
						}
						
						AgAction action = AgAction.NULL;
						final String pol = (String) policies.getSelectedItem();
						for (AgAction a : AgAction.values()) {
							if (a.toString().equals(pol)) {
								if (!gs.isTerminalState() && !gs.isObstacle() || a == AgAction.NULL) {
									action = a;
								}
								break;
							}
						}
						MDPView.this.mdpEnv.updateState(gs, (Double)reward.getValue(), isObstacle, isTerminalPositive, isTerminalNegative, action);
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
					MDPView.this.mdpEnv.askAgentToRun();
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
			policies.setEnabled(b);
			//run.setEnabled(b);
			reward.setEnabled(b);
			
		}
	}
	
	public class SizeDialog extends JFrame {
		final JPanel jpRow = new JPanel();
		final JPanel jpCol = new JPanel();
		final Coordinates size = MDPView.this.mdpModel.getSize();
		final JSpinner rowNumber = new JSpinner(new SpinnerNumberModel(size.y, 3, 100, 1));
		final JSpinner colNumber = new JSpinner(new SpinnerNumberModel(size.x, 3, 100, 1));
		
		public SizeDialog() {
			jpRow.setLayout(new GridLayout(1,2));
			jpCol.setLayout(new GridLayout(1,2));
			jpRow.add(new JLabel("Rows : "));
			jpRow.add(rowNumber);
			
			jpCol.add(new JLabel("Columns : "));
			jpCol.add(colNumber);
			JButton jbValidate = new JButton("Ok");
			jbValidate.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					int cols = (Integer)(colNumber.getValue());
					int rows = (Integer)(rowNumber.getValue());
					mdpEnv.setSize(new Coordinates(cols,rows));
					MDPView.this.dispose();
					SizeDialog.this.dispose();
					
				}
			});
			getContentPane().setLayout(new GridLayout(3, 1));
			add(jpRow);
			add(jpCol);
			add (jbValidate);
			
			pack();
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			setVisible(true);
			
			
		}
	}
	
}
