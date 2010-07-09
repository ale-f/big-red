package dk.itu.big_red.model;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

import dk.itu.big_red.exceptions.DuplicateControlException;

/**
 * The Signature is a central storage point for {@link Control}s and their
 * properties (both in terms of the bigraph model and their visual
 * representations). Every {@link Bigraph} has an associated Signature, which
 * they consult whenever they need to create a {@link Node}.
 * @author alec
 *
 */
public class Signature {
	public static Control DEFAULT_CONTROL =
		new Control("Unknown", "?", Control.Shape.SHAPE_POLYGON, Control.POINTS_QUAD, new Point(50, 50), true);
	
	private HashMap<String, Control> controls =
		new HashMap<String, Control>();
	
	public Signature() {
		addControl("Unknown", "?", Control.Shape.SHAPE_POLYGON, Control.POINTS_QUAD, new Point(50, 50), true);
	}
	
	public Control addControl(String longName, String label, Control.Shape shape, PointList points, Point defaultSize, boolean constraintModifiable) throws DuplicateControlException {
		Control m = null;
		if ((m = controls.get(longName)) == null) {
			m = new Control(longName, label, shape, points, defaultSize, constraintModifiable);
			controls.put(longName, m);
		}
		return m;
	}
	
	public void addControlsFrom(Signature i) {
		for (Control m : i.getControls()) {
			if (controls.get(m.getLongName()) == null)
				controls.put(m.getLongName(), m);
		}
	}
	
	public void removeControl(Control m) {
		if (m != null) {
			if (controls.get(m.getLongName()) != null)
				controls.remove(m.getLongName());
		}
	}
	
	public Control getControl(String name) {
		return controls.get(name);
	}

	public String[] getControlNames() {
		return controls.keySet().toArray(new String[0]);
	}
	
	public Collection<Control> getControls() {
		return controls.values();
	}
}