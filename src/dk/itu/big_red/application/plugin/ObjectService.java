package dk.itu.big_red.application.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dk.itu.big_red.utilities.ISafeCloneable;

public class ObjectService {
	public static interface UpdateListener {
		public void objectUpdated(Object identifier);
	}
	
	private Map<Object, ISafeCloneable> modelObjects =
			new HashMap<Object, ISafeCloneable>();
	private Map<Object, ArrayList<UpdateListener>> listeners =
		new HashMap<Object, ArrayList<UpdateListener>>();
	
	public ObjectService() {
		
	}
	
	/**
	 * Returns a copy of the {@link Object} registered with the given
	 * identifier.
	 * @param identifier an identifier
	 * @return a new copy of an existing {@link Object}, or <code>null</code>
	 */
	public ISafeCloneable getObject(Object identifier) {
		ISafeCloneable o = null;
		if ((o = modelObjects.get(identifier)) == null)
			/* no other handling as yet */;
		return (o != null ? o.clone() : null);
	}
	
	/**
	 * Associates a copy of the given {@link Object} with an identifier.
	 * @param identifier an identifier
	 * @param newValue the object to be copied and associated with the
	 * identifier
	 * @return <code>this</code>, for convenience
	 */
	public ObjectService setObject(Object identifier, ISafeCloneable newValue) {
		modelObjects.put(identifier, newValue.clone());
		ArrayList<UpdateListener> toNotify = listeners.get(identifier);
		if (toNotify != null)
			for (UpdateListener l : toNotify)
				l.objectUpdated(identifier);
		return this;
	}
	
	/**
	 * Registers an {@link UpdateListener} to receive change notifications for
	 * the given identifier.
	 * @param identifier an identifier
	 * @param l an {@link UpdateListener}
	 * @return <code>this</code>, for convenience
	 */
	public ObjectService addUpdateListener(Object identifier, UpdateListener l) {
		ArrayList<UpdateListener> toNotify = listeners.get(identifier);
		if (toNotify == null)
			listeners.put(identifier,
					toNotify = new ArrayList<UpdateListener>());
		toNotify.add(l);
		return this;
	}
	
	/**
	 * Unregisters the given {@link UpdateListener} from receiving change
	 * notifications for the given identifier. 
	 * @param identifier an identifier
	 * @param l an {@link UpdateListener}
	 * @return <code>this</code>, for convenience
	 */
	public ObjectService removeUpdateListener(Object identifier, UpdateListener l) {
		ArrayList<UpdateListener> toNotify = listeners.get(identifier);
		if (toNotify != null)
			toNotify.remove(l);
		return this;
	}
}
