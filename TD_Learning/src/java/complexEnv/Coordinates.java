package complexEnv;

import java.io.Serializable;
import java.util.Objects;

public class Coordinates implements Serializable{

	
	private static final long serialVersionUID = -5784692756272171316L;
	public int x,y;
	
	public Coordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public String toString() {
		return Integer.toString(x) + "," + Integer.toString(y);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (! (o instanceof Coordinates)) {
			return false;
		}
		Coordinates c = (Coordinates)(o);
		if (c.x != this.x || c.y != this.y) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x,y);
	}
}
