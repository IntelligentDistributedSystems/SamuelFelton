package complexEnv;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.SwingUtilities;

import complexEnv.MDPModel.AgAction;
import complexEnv.MDPModel.Direction;
import gui.frame.MDPView;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.environment.Environment;


/** Simple MDP board environment */
public class MDPEnv extends Environment {

	// Movement increments
	final int incRow[][] = { { 0, 1, 0, -1 }, { 1, 0, -1, 0 }, { 0, -1, 0, 1 } };

	final int incColumn[][] = { { -1, 0, 1, 0 }, { 0, 1, 0, -1 }, { 1, 0, -1, 0 } };
	private final String agentName = "agent1";
	private final Coordinates agentStartCoordinates = new Coordinates(1, 1);
	private Coordinates agentPosition = new Coordinates(agentStartCoordinates.x, agentStartCoordinates.y);

	// Agent current position. Initially agent is in start position.
	MDPView view;
	MDPModel model;
	Random r = new Random();

	@Override
	public void init(String[] args) {
		model = new MDPModel(new Coordinates(6, 5));
		model.addTerminalState(new Coordinates(4, 2), false);
		model.addTerminalState(new Coordinates(4, 3), true);
		model.addObstacle(new Coordinates(2, 2));
		updatePercepts();
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				view = new MDPView(MDPEnv.this, model);

			}
		});

	}

	/**
	 * sends the new environment policies and asks the agent to start the trial
	 */
	public void askAgentToRun() {
		updatePercepts();
		sendPolicies();
		addPercept(agentName, ASSyntax.createLiteral("must_run"));
	}
	/**
	 * Sends all the policies to the agent
	 */
	public void sendPolicies() {
		for (final Map.Entry<Coordinates, GridState> entry : model.getStates().entrySet()) {
			addPolicy(agentName, entry.getValue().getAction(), entry.getKey());
		}
	}

	/**
	 * Adds a policy to an agent
	 * 
	 * @param agName
	 *            the agent
	 * @param a
	 *            the agent
	 * @param row
	 *            the row
	 * @param col
	 *            the column
	 */
	public void addPolicy(String agName, AgAction a, Coordinates c) {
		final String action = getActionTextForAgent(a);
		final Literal s = ASSyntax.createLiteral("s", ASSyntax.createNumber(c.y), ASSyntax.createNumber(c.x));
		final Literal l = ASSyntax.createLiteral("policy", s, ASSyntax.createAtom(action));
		addPercept(agName, l);
	}
	private String getActionTextForAgent(AgAction a) {
		String dir = null;
		switch (a) {
		case LEFT:
			dir = "left";
			break;
		case RIGHT:
			dir = "right";
			break;
		case UP:
			dir = "up";
			break;
		case DOWN:
			dir = "down";
			break;
		case NULL:
			dir = "null";
			break;
		}
		return dir;
	}

	/**
	 * update the agent's percepts based on the current state of the world model
	 */
	private void updatePercepts() {
		clearPercepts(); // remove previous percepts
		double r = 0.0;
		GridState s = model.getState(agentPosition);

		r = s.getReward();
		Literal l = ASSyntax.createLiteral("pos", ASSyntax.createNumber(agentPosition.y),
				ASSyntax.createNumber(agentPosition.x), ASSyntax.createNumber(r),
				ASSyntax.createAtom(model.isExitState(agentPosition) ? "t" : "n"));
		addPercept(l);
	}

	/**
	 * Gets the utilities from the agent. Called by executeAction when the agent
	 * executes "send_policies" They must match this literal
	 * utility(s(row,Column),utility, trial)
	 * 
	 * @return true if it executed correctly
	 */
	public boolean getPolicies(String ag, Structure action) {
		if (action.getTerms() == null) {
			return false;
		}
		List<Term> listPolicies = ((ListTermImpl) (action.getTerm(0))).getAsList();
		for (final Term policy : listPolicies) {
			final Literal literalPolicy = (Literal) policy;
			final NumberTerm ntCol = (NumberTerm) literalPolicy.getTerm(0);
			final NumberTerm ntRow = (NumberTerm) literalPolicy.getTerm(1);
			final Atom act = (Atom) literalPolicy.getTerm(2);

			final Coordinates posState = new Coordinates(Integer.parseInt(ntRow.toString()),
					Integer.parseInt(ntCol.toString()));
			final GridState gs = model.getState(posState);
			if (gs == null) {
				System.out.println("gs is null");

			}
			String stringAct = act.toString();
			if (stringAct.equals("left")) {
				gs.setAction(AgAction.LEFT);
			} else if (stringAct.equals("right")) {
				gs.setAction(AgAction.RIGHT);
			} else if (stringAct.equals("up")) {
				gs.setAction(AgAction.UP);
			} else if (stringAct.equals("down")) {
				gs.setAction(AgAction.DOWN);
			} else {
				gs.setAction(AgAction.NULL);
			}

		}

		return true;
	}

	/**
	 * Gets the utilities from the agent. Called by executeAction when the agent
	 * executes "send_utilities" They must match this literal
	 * utility(s(row,Column),utility, trial)
	 * 
	 * @return true if it executed correctly
	 */
	private boolean getUtilities(String ag, Structure action) {
		if (action.getTerms() == null) {
			return false;
		}
		final List<Term> listUtilities = ((ListTermImpl) (action.getTerm(0))).getAsList();
		for (final Term utility : listUtilities) {
			final Literal literalUtility = (Literal) utility;
			final Literal posLiteral = (Literal) literalUtility.getTerm(0);
			final NumberTerm ntRow = (NumberTerm) posLiteral.getTerm(1);
			final NumberTerm ntCol = (NumberTerm) posLiteral.getTerm(0);
			final NumberTerm u = (NumberTerm) literalUtility.getTerm(1);
			final NumberTerm t = (NumberTerm) literalUtility.getTerm(2);

			final Coordinates posState = new Coordinates(Integer.parseInt(ntRow.toString()),
					Integer.parseInt(ntCol.toString()));
			final GridState gs = model.getState(posState);
			gs.setTrialNumber(Integer.parseInt(t.toString()));
			gs.setUtility(Double.parseDouble(u.toString()));

		}
		return true;
	}
	private boolean moveAgent(String ag, Structure action) {
		final Coordinates size = model.getSize();
		final int d = r.nextInt(1000);
		Direction direction;
		AgAction actionId;
		final Coordinates newAgentPosition = new Coordinates(0, 0);
		if (d < 100) {
			direction = Direction.CHANGE_LEFT;
		} else if (d < 900) {
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
			agentPosition = newAgentPosition;
		}
		
		return true;

	}
	/** changes the world model according to agent actions */
	@Override
	public boolean executeAction(String ag, Structure action) {
		
		if (action.getFunctor().equals("send_policies")) {
			getPolicies(ag, action);
		} else if (action.getFunctor().equals("send_utilities")) {
			getUtilities(ag, action);
		} else {
			moveAgent(ag, action);
		}
		
		updatePercepts(); // update the agent's percepts for the new
							// state of the world (after this action)
		return true; // all actions succeed
	}

	public void save(File f) throws SaveException{
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
	public void updateState(GridState gs, double reward, boolean isObstacle, boolean isTerminalPositive, boolean isTerminalNegative, AgAction a)
	{
		gs.setObstacle(isObstacle);
		gs.setPositiveTerminalState(isTerminalPositive);
		gs.setNegativeTerminalState(isTerminalNegative);
		gs.setAction(a);
	}
}
