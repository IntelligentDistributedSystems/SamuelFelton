package mdp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import mdp.MDPModel.AgAction;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AgentData {
	private String agName;
	
	private Map<MapKey,List<Double>> qValues;
	private Map<MapKey,List<Integer>> trialNumbers;
	
	private Coordinates agentPosition;
	
	public AgentData(String agName,Coordinates pos) {
		this.agName = agName;
		this.agentPosition = pos;
		qValues = new HashMap<>();
		trialNumbers = new HashMap<>();
	}
	public Coordinates getAgentPosition() {
		return agentPosition;
	}
	public void addQValue(Coordinates c, AgAction a, Double d) {
		MapKey m = new MapKey(c, a);
		List<Double> values = qValues.get(m);
		if (values == null) {
			qValues.put(m, new ArrayList<Double>());
			values = qValues.get(m);
		}
		values.add(d);
	}
	public void addTrial(Coordinates c, AgAction a, Integer i) {
		MapKey m = new MapKey(c, a);
		List<Integer> values = trialNumbers.get(m);
		if (values == null) {
			trialNumbers.put(m, new ArrayList<Integer>());
			values = trialNumbers.get(m);
		}
		values.add(i);
	}
	
	protected class MapKey implements Comparable<MapKey>{
		private AgAction action;
		private Coordinates coordinates;
		public MapKey(Coordinates coord, AgAction a) {
			coordinates = coord;
			action = a;
		}
		@Override
		public int hashCode() {
			return Objects.hash(coordinates,action);
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MapKey other = (MapKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (action != other.action)
				return false;
			if (coordinates == null) {
				if (other.coordinates != null)
					return false;
			} else if (!coordinates.equals(other.coordinates))
				return false;
			return true;
		}
		private AgentData getOuterType() {
			return AgentData.this;
		}
		public AgAction getAction() {
			return action;
		}
		public Coordinates getCoordinates() {
			return coordinates;
		}
		public void setAction(AgAction action) {
			this.action = action;
		}
		public void setCoordinates(Coordinates coordinates) {
			this.coordinates = coordinates;
		}
		@Override
		public int compareTo(MapKey m) {
			return (coordinates.hashCode() +action.value()) - (m.coordinates.hashCode() +m.action.value());
			//return (coordinates.x - m.coordinates.x) * 31 + (coordinates.y - m.coordinates.y) * 7 + (action.value() - m.action.value()) ;
		}
		
	}

	public void setAgentPosition(Coordinates agentPosition) {
		this.agentPosition = agentPosition;
	}
	public String getQValuesCSV() {
		final StringBuilder sb = new StringBuilder();
		for (final MapKey mp : qValues.keySet()) {
			final List<Double> values = qValues.get(mp);
			final List<Integer> trials = trialNumbers.get(mp);
			if (values != null && !values.isEmpty() && trials != null && !trials.isEmpty()) {
				sb.append(mp.coordinates.y + " " + mp.coordinates.x + " " + mp.action); // premier element de la ligne = position + action
				for (int i = 0; i < values.size(); i++) {
					sb.append(";").append(values.get(i)).append(" ").append(trials.get(i));
				}
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	
}
