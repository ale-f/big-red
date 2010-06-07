package dk.itu.big_red.model;

import java.util.ArrayList;
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
	
	private HashMap<String, Control> classes =
		new HashMap<String, Control>();
	public HashMap<String, ArrayList<String>> connections =
		new HashMap<String, ArrayList<String>>();
	public HashMap<String, String> comments =
		new HashMap<String, String>();
	
	public Signature() {
		registerControl("Unknown", "?", Control.Shape.SHAPE_RECTANGLE, new Point(50, 50), true);
	}
	
	public Control registerControl(String longName, String label, Control.Shape shape, Point defaultSize, boolean constraintModifiable) throws DuplicateControlException {
		Control m = null;
		if ((m = classes.get(longName)) == null) {
			m = new Control(longName, label, shape, defaultSize, constraintModifiable);
			classes.put(longName, m);
		}
		return m;
	}
	
	public void registerControlsFrom(Signature i) {
		for (Control m : i.getControls()) {
			if (classes.get(m.getLongName()) == null)
				classes.put(m.getLongName(), m);
		}
	}
	
	public Control getControl(String name) {
		return classes.get(name);
	}

	public String[] getControlNames() {
		return classes.keySet().toArray(new String[0]);
	}
	
	public Collection<Control> getControls() {
		return classes.values();
	}
	
	public void deleteControl(Control m) {
		if (m != null) {
			if (classes.get(m.getLongName()) != null)
				classes.remove(m.getLongName());
		}
	}
	
	public boolean isKeyRegistered(String key) {
		return connections.get(key) != null;
	}
	
	public void registerKey(String key) {
		if (key != null && !isKeyRegistered(key))
			connections.put(key, new ArrayList<String>());
	}
	
	public boolean canConnect(String key1, String key2) {
		return (isKeyRegistered(key1) && isKeyRegistered(key2) &&
				connections.get(key1).contains(key2) &&
				connections.get(key2).contains(key1));
	}
	
	/**
	 * Allows connections to be established between the two specified kinds of
	 * Port.
	 * @param key1
	 * @param key2
	 */
	public void allowConnection(String key1, String key2) {
		registerKey(key1);
		registerKey(key2);
		if (!connections.get(key1).contains(key2))
			connections.get(key1).add(key2);
		if (!connections.get(key2).contains(key1))
			connections.get(key2).add(key1);
	}
	
	public void denyConnection(String key1, String key2) {
		if (canConnect(key1, key2)) {
			connections.get(key1).remove(key2);
			connections.get(key2).remove(key1);
		}
	}
	
	public Collection<String> getPorts() {
		return connections.keySet();
	}
	
	public Collection<String> getConnections(String key) {
		return connections.get(key);
	}
	
	public void setComment(String key, String comment) {
		registerKey(key);
		comments.put(key, comment);
	}
	
	public String getComment(String key) {
		return comments.get(key);
	}
	
	public void registerPortsFrom(Signature i) {
		for (String p : i.getPorts()) {
			registerKey(p);
			for (String k : i.getConnections(p))
				allowConnection(p, k);
		}
	}

	public void clearConnections(String key) {
		Collection<String> c = getConnections(key);
		if (c != null) {
			for (String c1 : c)
				connections.get(c1).remove(key);
			c.clear();
		}
	}
}