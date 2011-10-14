package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import dk.itu.big_red.model.assistants.CloneMap;
import dk.itu.big_red.model.interfaces.internal.IPropertyChangeNotifier;

/**
 * This is the superclass of everything in Big Red's version of the bigraphical
 * model. It provides an implementation of {@link IPropertyChangeNotifier}, and
 * allows for a {@link String} comment to be set and retrieved.
 * 
 * <p>Objects which can appear on a bigraph are instances of the subclass
 * {@link Layoutable}.
 * @author alec
 * @see Layoutable
 *
 */
public class ModelObject implements IPropertyChangeNotifier {
	private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	@Override
	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public final void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	/**
	 * Notifies all associated {@link PropertyChangeListener}s of a property
	 * change.
	 * @param propertyName the ID of the changed property
	 * @param oldValue its old value
	 * @param newValue its new value
	 */
	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
		listeners.firePropertyChange(propertyName, oldValue, newValue);
	}
	
	/**
	 * Returns a new instance of this {@link ModelObject}'s class,
	 * created as though by <code>this.getClass().newInstance()</code>.
	 * @return a new instance of this ModelObject's class, or
	 * <code>null</code>
	 */
	protected ModelObject newInstance() {
		try {
			return getClass().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Creates and returns a new copy of this {@link ModelObject}.
	 * <p>(Although the returned copy is a {@link ModelObject}, it's
	 * really an instance of whatever subclass this object is.)
	 * @param m a {@link CloneMap} to be notified of the new copy, or
	 * <code>null</code>
	 * @return a new copy of this {@link ModelObject}
	 */
	public ModelObject clone(CloneMap m) {
		ModelObject i = newInstance();
		if (m != null)
			m.setCloneOf(this, i);
		return i;
	}
	
	private String comment = null;
	
	/**
	 * The property name fired when the comment changes.
	 */
	public static final String PROPERTY_COMMENT = "ICommentableComment";
	
	/**
	 * Returns the current comment for this object.
	 * @return the current comment
	 */
	public String getComment() {
		return comment;
	}
	
	/**
     * Changes this object's comment.
     * @param comment the new comment
     */
	public void setComment(String comment) {
		String oldComment = this.comment;
		this.comment = comment;
		firePropertyChange(ModelObject.PROPERTY_COMMENT, oldComment, comment);
	}
}
