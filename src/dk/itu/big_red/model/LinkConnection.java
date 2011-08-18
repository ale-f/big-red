package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.Connection;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.interfaces.internal.IPropertyChangeNotifier;

/**
 * An LinkConnection is the model object behind an actual {@link Connection}
 * on the bigraph. {@link Link}s create and manage them.
 * <p>The target of a {@link LinkConnection} is always a <code>Link</code>, and
 * its source is always a <code>Point</code>.
 * @author alec
 *
 */
public class LinkConnection implements IPropertyChangeNotifier, IAdaptable {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	/**
	 * The property name fired when the connection source changes.
	 */
	public static final String PROPERTY_SOURCE = "LinkConnectionSource";
	/**
	 * The property name fired when the connection target changes.
	 */
	public static final String PROPERTY_TARGET = "LinkConnectionTarget";
	
	private Point source;
	private Link target;
	
	public LinkConnection(Link link) {
		this.target = link;
	}
	
	/**
	 * Sets the source of this connection to the given {@link IConnectable}.
	 * <p>(There's no corresponding way of setting the <i>target</i> of this
	 * connection because it's always the same - {@link #getTarget()}).
	 * @param source the new source
	 */
	public void setSource(Point source) {
		if (source != null) {
			Point oldSource = this.source;
			this.source = source;
			listeners.firePropertyChange(PROPERTY_SOURCE, oldSource, source);
		}
	}
	
	/**
	 * Gets the {@link Point} that is the source of this Connection.
	 * @return the current source
	 */
	public Point getSource() {
		return this.source;
	}
	
	/**
	 * Gets the {@link Link} which manages and contains this connection.
	 * @return the target Link
	 */
	public Link getTarget() {
		return target;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new ModelPropertySource(getTarget());
		} else return null;
	}
}
