package dk.itu.big_red.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
	public static final String[] EMPTY_STRING_ARRAY = new String[]{};
	public static final Control DEFAULT_CONTROL = new Control();
	
	private ArrayList<Control> controls = new ArrayList<Control>();
	
	public Signature() {
		controls.add(DEFAULT_CONTROL);
	}
	
	public Control addControl(Control c) throws DuplicateControlException {
		Control m = null;
		if ((m = getControl(c.getLongName())) == null) {
			controls.add(m = c);
			c.setSignature(this);
		} else throw new DuplicateControlException(c.getLongName());
		return m;
	}
	
	public void removeControl(Control m) {
		if (controls.contains(m) && m != DEFAULT_CONTROL) {
			controls.remove(m);
			m.setSignature(null);
		}
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