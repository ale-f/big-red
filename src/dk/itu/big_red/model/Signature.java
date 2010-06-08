package dk.itu.big_red.model;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.draw2d.geometry.Point;

import dk.itu.big_red.exceptions.DuplicateControlException;

/**
 * The ControlAuthority is a central storage point for controls and their
 * properties; every {@link Bigraph} (that is, every document) has an
 * associated ControlAuthority.
 * 
 * <p>Make sure the controls of all the nodes you're using have been
 * registered with the containing Bigraph!
 * @author alec
 *
 */
public class Signature {
	public static Control DEFAULT_CONTROL =
		new Control("Unknown", "?", Control.Shape.SHAPE_RECTANGLE, new Point(50, 50), true);
	
	private HashMap<String, Control> controls =
		new HashMap<String, Control>();
	
	public Signature() {
		addControl("Unknown", "?", Control.Shape.SHAPE_RECTANGLE, new Point(50, 50), true);
	}
	
	public Control addControl(String longName, String label, Control.Shape shape, Point defaultSize, boolean constraintModifiable) throws DuplicateControlException {
		Control m = null;
		if ((m = controls.get(longName)) == null) {
			m = new Control(longName, label, shape, defaultSize, constraintModifiable);
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