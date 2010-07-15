package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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
	public static String[] EMPTY_STRING_ARRAY = new String[]{};
	public static Control DEFAULT_CONTROL =
		new Control("Unknown", "?", Control.Shape.SHAPE_POLYGON, Control.POINTS_QUAD, new Point(50, 50), true);
	
	private ArrayList<Control> controls = new ArrayList<Control>();
	
	public Signature() {
		addControl(DEFAULT_CONTROL);
	}
	
	public Control addControl(Control c) throws DuplicateControlException {
		Control m = null;
		if ((m = getControl(c.getLongName())) == null)
			controls.add(m = c);
		return m;
	}
	
	public void addControlsFrom(Signature i) {
		for (Control c : i.getControls()) {
			if (getControl(c.getLongName()) == null)
				controls.add(c);
		}
	}
	
	public void removeControl(Control m) {
		if (controls.contains(m))
			controls.remove(m);
	}
	
	public Control getControl(String name) {
		for (Control c : controls)
			if (c.getLongName().equals(name))
				return c;
		return null;
	}

	public String[] getControlNames() {
		ArrayList<String> controlNames = new ArrayList<String>();
		for (Control c : controls)
			controlNames.add(c.getLongName());
		return controlNames.toArray(EMPTY_STRING_ARRAY);
	}
	
	public List<Control> getControls() {
		return controls;
	}
}