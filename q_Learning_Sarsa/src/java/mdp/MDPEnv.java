package mdp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.SwingUtilities;

import gui.frame.MDPView;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;
import mdp.MDPModel.AgAction;
import mdp.MDPModel.Direction;

/** Simple MDP board environment */
public class MDPEnv extends Environment {

	private final String uiUpdater = "qAgent1";
	// Movement increments
	final int incRow[][] = { { 0, 1, 0, -1 }, { 1, 0, -1, 0 }, { 0, -1, 0, 1 } };

	final int incColumn[][] = { { -1, 0, 1, 0 }, { 0, 1, 0, -1 }, { 1, 0, -1, 0 } };

	private final Coordinates agentStartCoordinates = new Coordinates(1, 1);
	private boolean agentsInitialized = false;
	private final Map<String, AgentData> agentDatas = new HashMap<>();
	private MDPView view;
	private MDPModel model;
	private Random r = new Random();

	@Override
	public void init(String[] args) {

		model = new MDPModel(new Coordinates(6, 5));
		model.addTerminalState(new Coordinates(4, 2), false);
		model.addTerminalState(new Coordinates(4, 3), true);
		model.addObstacle(new Coordinates(2, 2));

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				view = new MDPView(MDPEnv.this, model);
				System.out
						.println("Number of agents : " + getEnvironmentInfraTier().getRuntimeServices().getAgentsQty());
			}
		});

	}

	/**
	 * sends the new environment policies and asks the agent to start the trial
	 */
	public void askAgentToRun(String agName) {
		updatePercepts(agName);
		addPercept(agName, ASSyntax.createLiteral("must_run"));
	}

	public void askAgentsToRun() {
		
		Set<String> agentNames = getEnvironmentInfraTier().getRuntimeServices().getAgentsNames();
		for (String agName : agentNames) {
			// create the data storage if it's not there, or recreate it to
			// reinitialize the run
			agentDatas.put(agName, new AgentData(agName, agentStartCoordinates));
			updatePercepts(agName);
			addPercept(agName, ASSyntax.createLiteral("must_run"));
		}
		

	}

	/**
	 * update the agent's percepts based on the current state of the world model
	 */
	private void updatePercepts(String agName) {
		clearPercepts();
		clearPercepts(agName); // remove previous percepts
		final Coordinates agentPosition = agentDatas.get(agName).getAgentPosition();
		final GridState s = model.getState(agentPosition);
		final double r = s.getReward();
		final Literal l = ASSyntax.createLiteral("pos", ASSyntax.createNumber(agentPosition.y),
				ASSyntax.createNumber(agentPosition.x), ASSyntax.createNumber(r),
				ASSyntax.createAtom(model.isExitState(agentPosition) ? "t" : "n"));
		addPercept(agName, l);
	}

	/**
	 * Gets the q-values from the agent. Called by executeAction when the agent
	 * executes "send_q_values" They must match this literal
	 * qvalue(s(row,Column,n || t),Action,QValues, trial)
	 * 
	 * @return true if it executed correctly
	 */
	public boolean getQValues(String ag, Structure action) {
		if (action.getTerms() == null) {
			return false;
		}
		final List<Term> listValues = ((ListTermImpl) (action.getTerm(0))).getAsList();
		for (final Term valueStruct : listValues) {
			final Literal literalValue = (Literal) valueStruct;
			final Literal posLiteral = (Literal) literalValue.getTerm(0);
			final NumberTerm ntRow = (NumberTerm) posLiteral.getTerm(1);
			final NumberTerm ntCol = (NumberTerm) posLiteral.getTerm(0);
			final Atom actionAtom = (Atom) literalValue.getTerm(1);
			final NumberTerm q = (NumberTerm) literalValue.getTerm(2);
			final NumberTerm t = (NumberTerm) literalValue.getTerm(3);

			final Coordinates posState = new Coordinates(Integer.parseInt(ntRow.toString()),
					Integer.parseInt(ntCol.toString()));
			final GridState gs = model.getState(posState);
			AgAction act = null;
			for (AgAction val : AgAction.values()) {
				if (val.toString().equals(actionAtom.toString().toUpperCase())) {
					act = val;
					break;
				}
			}
			Integer trialNumber = Integer.parseInt(t.toString());
			Double qValue = Double.parseDouble(q.toString());
			AgentData data = agentDatas.get(ag);

			data.addQValue(posState, act, qValue);
			data.addTrial(posState, act, trialNumber);

			if (ag.equals(uiUpdater)) {
				gs.setTrialNumbers(act, trialNumber);
				gs.setQValue(act, qValue);

			}
		}
		return true;
	}

	/** changes the world model according to agent actions */
	@Override
	public boolean executeAction(String ag, Structure action) {

		final int d = r.nextInt(1000);
		Direction direction;
		final Coordinates size = model.getSize();
		if (action.getFunctor().equals("send_q_values")) {
			getQValues(ag, action);
		}
		if (action.getFunctor().equals("request_update")) {
			System.out.println("UPDATE REQUESTED BY " + ag);
			updatePercepts(ag);
		}
		AgAction actionId;
		Coordinates agentPosition = agentDatas.get(ag).getAgentPosition();
		final Coordinates newAgentPosition = new Coordinates(0, 0);
		if (d < 10) {
			direction = Direction.CHANGE_LEFT;
		} else if (d < 990) {
			direction = Direction.KEEP;
		} else {
			direction = Direction.CHANGE_RIGHT;
		}

		actionId = AgAction.NULL;
		if (action.getFunctor().equals("up")) {
			actionId = AgAction.UP;
		} else if (action.getFunctor().equals("right")) {
			actionId = AgAction.RIGHT;
		} else if (action.getFunctor().equals("down")) {
			actionId = AgAction.DOWN;
		} else if (action.getFunctor().equals("left")) {
			actionId = AgAction.LEFT;
		}

		if (actionId != AgAction.NULL) {
			newAgentPosition.x = agentPosition.x + incColumn[direction.value()][actionId.value()];
			newAgentPosition.y = agentPosition.y + incRow[direction.value()][actionId.value()];
		} else {

			do {
				newAgentPosition.x = r.nextInt(size.x - 2) + 1;
				newAgentPosition.y = r.nextInt(size.y - 2) + 1;
			} while (model.getState(newAgentPosition).isObstacle() == true);

		}

		if (!model.getState(newAgentPosition).isObstacle()) {
			agentDatas.get(ag).setAgentPosition(newAgentPosition);
		}

		updatePercepts(ag); // update the agent's percepts for the new
							// state of the world (after this action)
		return true; // all actions succeed
	}

	public void save(File f) {
		model.save(f);
	}

	/*
	 * Loads the environment in the file f
	 */
	public void load(File f) {
		model.load(f);
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				if (view != null) {
					view.dispose();
				}
				view = new MDPView(MDPEnv.this, model);

			}
		});
	}

	public void saveQValuesAsCSV(String baseName) {
		final Set<String> agentNames = getEnvironmentInfraTier().getRuntimeServices().getAgentsNames();
		for (final String agName : agentNames) {
			StringBuilder s = new StringBuilder();
			AgentData ad = agentDatas.get(agName);
			if (ad != null) {

				s.append(agentDatas.get(agName).getQValuesCSV());
				String name = "results/csv/" + baseName + "_" + agName + ".csv";
				File f = new File(name);

				try (PrintWriter ps = new PrintWriter(f)) {
					ps.write(s.toString());
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
		}

	}

	public void generateXLS(String baseName) {
		Process ps = null;
		String pathForExp = baseName;
		final Set<String> agentNames = getEnvironmentInfraTier().getRuntimeServices().getAgentsNames();
		String[] agNamesArray = new String[agentNames.size()];
		agentNames.toArray(agNamesArray);
		String[] defaultParams = new String[] { "tools/chartGen.exe" };
		for (int i = 0; i < agNamesArray.length; ++i) {
			agNamesArray[i] = pathForExp + "_" + agNamesArray[i] + ".csv";
		}
		for (String a : agNamesArray) {
			System.out.println(a);
		}
		String[] params = new String[defaultParams.length + agNamesArray.length];
		System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
		System.arraycopy(agNamesArray, 0, params, defaultParams.length, agNamesArray.length);
		
		try {
			ps = Runtime.getRuntime().exec(params);
			ps.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the size of environment and reopens a new view. If the envrionment
	 * is now bigger, the added states are empty and have a 0.0 reward. The
	 * ancient states are kept. The borders are recreated.
	 * 
	 * @param newSize
	 */
	public void setSize(Coordinates newSize) {

		model.setSize(newSize);
		view.dispose();
		view = new MDPView(this, model);
	}
	
	
	private void sendEpsilons() {
		System.out.println("SENT");
		addPercept("sarsa0",ASSyntax.createLiteral("epsilon", new NumberTermImpl(0)));
		addPercept("sarsaDot1",Literal.parseLiteral("epsilon(0.1)"));
		addPercept("sarsaDot5",Literal.parseLiteral("epsilon(0.5)"));
		addPercept("sarsaDot8",Literal.parseLiteral("epsilon(0.8)"));
		addPercept("sarsa1",Literal.parseLiteral("epsilon(1)"));
	}
}
