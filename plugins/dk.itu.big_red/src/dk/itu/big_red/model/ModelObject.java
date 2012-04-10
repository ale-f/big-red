package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.services.IDisposable;

import dk.itu.big_red.model.assistants.IPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.changes.Change;

/**
 * This is the superclass of everything in Big Red's version of the bigraphical
 * model. It allows {@link PropertyChangeListener}s to register for, and
 * unregister from, change notifications, and has a {@link String} comment
 * which can be set and retrieved.
 * 
 * <p>Objects which can appear on a bigraph are instances of the subclass
 * {@link Layoutable}.
 * @author alec
 * @see Layoutable
 *
 */
public abstract class ModelObject implements IDisposable, IPropertyProvider {
	/**
	 * The property name fired when the comment changes.
	 */
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_COMMENT = "ModelObjectComment";
	
	public abstract class ModelObjectChange extends Change {
		/**
		 * Gets the {@link ModelObject} which created this {@link ModelObjectChange}.
		 * @return
		 */
		public ModelObject getCreator() {
			return ModelObject.this;
		}
	}
	
	public class ChangeComment extends ModelObjectChange {
		public String comment;
		
		protected ChangeComment(String comment) {
			this.comment = comment;
		}

		private boolean oldCommentSet = false;
		private String oldComment;
		
		@Override
		public void beforeApply() {
			oldComment = getCreator().getComment();
			oldCommentSet = true;
		}
		
		@Override
		public boolean canInvert() {
			return oldCommentSet;
		};
		
		@Override
		public ChangeComment inverse() {
			return new ChangeComment(oldComment);
		}
	}
	
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	/**
	 * Registers a {@link PropertyChangeListener} to receive property change
	 * notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	/**
	 * Unregisters a {@link PropertyChangeListener} from receiving property
	 * change notifications from this object.
	 * @param listener the PropertyChangeListener
	 */
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
	public ModelObject newInstance() {
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
	public ModelObject clone(Map<ModelObject, ModelObject> m) {
		ModelObject i = newInstance();
		if (m != null)
			m.put(this, i);
		i.setComment(getComment());
		return i;
	}
	
	@Override
	public ModelObject clone() {
		return clone(null);
	}
	
	private IFile file = null;
	
	public IFile getFile() {
		return file;
	}
	
	public ModelObject setFile(IFile file) {
		this.file = file;
		return this;
	}
	
	private String comment = null;
	
	/**
	 * Returns the current comment for this object.
	 * @return the current comment
	 */
	public String getComment() {
		return comment;
	}
	
	public String getComment(IPropertyProviderProxy context) {
		return (String)getProperty(context, PROPERTY_COMMENT);
	}
	
	/**
     * Changes this object's comment.
     * @param comment the new comment
     */
	protected void setComment(String comment) {
		String oldComment = this.comment;
		this.comment = comment;
		firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}
	
	@Override
	public Object getProperty(String name) {
		if (PROPERTY_COMMENT.equals(name)) {
			return getComment();
		} else return null;
	}
	
	protected Object getProperty(IPropertyProviderProxy context, String name) {
		return (context == null ?
				getProperty(name) : context.getProperty(this, name));
	}
	
	@Override
	public String toString() {
		return "<" + getType() + "@" + System.identityHashCode(this) + ">";
	}
	
	/**
	 * Returns the name of this object's type.
	 * @return the name, as a {@link String}
	 */
	public String getType() {
		return getClass().getSimpleName();
	}
	
	public ChangeComment changeComment(String comment) {
		return new ChangeComment(comment);
	}

	@Override
	public void dispose() {
		comment = null;
		
		PropertyChangeListener[] pls =
			listeners.getPropertyChangeListeners().clone();
		for (PropertyChangeListener i : pls)
			listeners.removePropertyChangeListener(i);
		listeners = null;
		
		file = null;
	}
}
