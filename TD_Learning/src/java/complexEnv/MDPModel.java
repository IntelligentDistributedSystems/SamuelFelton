package complexEnv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.SwingUtilities;

import gui.frame.MDPView;

public class MDPModel {
	private Coordinates size = new Coordinates(6, 5);
	private Map<Coordinates, GridState> states = new HashMap<Coordinates, GridState>();

	//default rewards
	private final double rw = -0.04;
	private final double obstacleRw = 0.0;
	private final double positiveTerminalRw = 1.0;
	private final double negativeTerminalRw = -1.0;


	private final Predicate<Coordinates> isBorder = new Predicate<Coordinates>() {

		public boolean test(Coordinates c) {
			return c.x == 0 || c.x == size.x - 1 || c.y == 0 || c.y == size.y - 1;
		}
	};

	// Direction can be:
	// - left to the move direction
	// - keep the move direction
	// - right to the move direction
	public enum Direction {
		CHANGE_LEFT(0), KEEP(1), CHANGE_RIGHT(2);
		private int value;

		public int value() {
			return value;
		}

		private Direction(int value) {
			this.value = value;
		}
	};

	// Agent actions
	public enum AgAction {
		DOWN(0), RIGHT(1), UP(2), LEFT(3), NULL(4);
		private int value;

		public int value() {
			return value;
		}

		public AgAction reverse() {
			switch (value) {
			case 3:
				return RIGHT;
			case 1:
				return LEFT;
			case 0:
				return DOWN;
			case 2:
				return UP;
			}
			return NULL;
		}

		private AgAction(int value) {
			this.value = value;
		}
	};

	public MDPModel(Coordinates size) {
		this.size = size;
		initEmptyEnvironment();
	}

	/**
	 * creates an empty environment with borders
	 */
	private void initEmptyEnvironment() {
		for (int i = 0; i < size.x; ++i) {
			for (int j = 0; j < size.y; ++j) {
				final Coordinates c = new Coordinates(i, j);
				GridState gs = new GridState(rw, false);
				if (isBorder.test(c)) {
					gs.setObstacle(true);
					gs.setReward(obstacleRw);
				}
				states.put(c, gs);
			}
		}

	}

	/**
	 * Adds a terminal state, positive or negative at the coordinates
	 * 
	 * @param c
	 *            the coordinates
	 * @param isPositive
	 */
	public void addTerminalState(Coordinates c, boolean isPositive) {
		final GridState gs = states.get(c);
		Objects.requireNonNull(gs, "State does not exist");
		if (gs.isTerminalState()) {

			System.out.println("Terminal state " + c + " Already added, WARNING !");
		} else {
			gs.addTerminalState(isPositive);
			gs.setReward(isPositive ? positiveTerminalRw : negativeTerminalRw);

		}
	}

	/**
	 * Adds an obstacle at the coordinates
	 * 
	 * @param c
	 */
	public void addObstacle(Coordinates c) {
		final GridState gs = states.get(c);
		Objects.requireNonNull(gs);
		gs.setObstacle(true);
		gs.setReward(obstacleRw);
	}

	public Coordinates getSize() {
		return size;
	}

	public GridState getState(Coordinates c) {
		return states.get(c);
	}
	public Map<Coordinates,GridState> getStates() {
		return states;
	}

	/**
	 * Saves this environment (size, states) into the file
	 * 
	 * @param f
	 *            the file
	 */
	public void save(File f) throws SaveException {
		Objects.requireNonNull(f);
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f))) {
			oos.writeObject(size);
			oos.writeObject(states);
		} catch (FileNotFoundException e) {
			throw new SaveException("The file was not found !");
		} catch (IOException e) {
			throw new SaveException("I/O Exception");
		}
	}

	/*
	 * Loads the environment in the file f
	 */
	public void load(File f) {
		try (ObjectInputStream oos = new ObjectInputStream(new FileInputStream(f))) {
			size = (Coordinates) oos.readObject();
			states = (Map<Coordinates, GridState>) oos.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
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

		// delete
		for (int i = size.x; i > newSize.x; i--) {
			for (int j = 0; j < size.y; j++) {
				states.remove(new Coordinates(i, j));
			}
		}
		for (int i = size.y; i > newSize.y; i--) {
			for (int j = 0; j < size.x; j++) {
				states.remove(new Coordinates(j, i));
			}
		}

		// add
		for (int i = size.x; i < newSize.x; i++) {
			for (int j = 0; j < newSize.y; j++) {
				states.put(new Coordinates(i, j), new GridState(0.0, false));
			}
		}
		for (int i = size.y; i < newSize.y; i++) {
			for (int j = 0; j < newSize.x; j++) {
				states.put(new Coordinates(j, i), new GridState(0.0, false));
			}
		}
		recreateBorders(newSize);
		size = newSize;

	}

	// Checks if a position {4,c) is a terminal state
	public boolean isExitState(Coordinates c) {

		GridState gs = states.get(c);
		if (gs == null) {
			return false;
		} else {
			return gs.isTerminalState();
		}

	}

	/**
	 * Recreates the borders according to the new size of the environment
	 * 
	 * @param newSize
	 */
	private void recreateBorders(final Coordinates newSize) {
		final Predicate<Coordinates> isNewBorder = new Predicate<Coordinates>() {

			public boolean test(Coordinates c) {
				return c.x == 0 || c.x == newSize.x - 1 || c.y == 0 || c.y == newSize.y - 1;
			}
		};
		final Stream<Coordinates> oldBorders = states.keySet().stream().filter(isBorder);
		final Stream<Coordinates> newBorders = states.keySet().stream().filter(isNewBorder);

		oldBorders.forEach(new Consumer<Coordinates>() {
			public void accept(Coordinates t) {
				states.get(t).setObstacle(false);
			}

		});

		newBorders.forEach(new Consumer<Coordinates>() {
			public void accept(Coordinates t) {
				states.get(t).setObstacle(true);

			}
		});

	}
}
