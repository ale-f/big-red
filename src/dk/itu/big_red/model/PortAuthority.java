package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * The PortAuthority is, like the {@link ControlAuthority}, a central storage
 * point for ports.
 * @author alec
 *
 */
public class PortAuthority {
	public HashMap<String, ArrayList<String>> connections =
		new HashMap<String, ArrayList<String>>();
	public HashMap<String, String> comments =
		new HashMap<String, String>();
	
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
	
	public PortAuthority() {
	}
	
	public void registerPortsFrom(PortAuthority i) {
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
