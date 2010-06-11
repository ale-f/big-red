package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.draw2d.Connection;

import dk.itu.big_red.model.interfaces.ICommentable;
import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;

/**
 * An EdgeConnection is the model object behind an actual {@link Connection}
 * on the bigraph. {@link Edge}s create and manage them.
 * @author alec
 *
 */
public class EdgeConnection implements IPropertyChangeNotifier, ICommentable {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

	/**
	 * The property name fired when the connection source changes.
	 */
	public static final String PROPERTY_SOURCE = "EdgeConnectionSource";
	/**
	 * The property name fired when the connection target changes.
	 */
	public static final String PROPERTY_TARGET = "EdgeConnectionTarget";
	
	private IConnectable source;
	private Edge parent;
	
	public EdgeConnection(Edge parent) {
		this.parent = parent;
	}
	
	/**
	 * Sets the source of this connection to the given {@link IConnectable}.
	 * <p>(There's no corresponding way of setting the <i>target</i> of this
	 * connection because it's always the same - {@link Edge#getEdgeTarget()
	 * getParent().getEdgeTarget()}).
	 * @param source the new source
	 */
	public void setSource(IConnectable source) {
		if (source != null) {
			IConnectable oldSource = this.source;
			this.source = source;
			listeners.firePropertyChange(PROPERTY_SOURCE, oldSource, source);
		}
	}
	
	/**
	 * Gets the {@link IConnectable} that is the source of this Connection.
	 * @return the current source
	 */
	public IConnectable getSource() {
		return this.source;
	}
	
	/**
	 * Gets the {@link Edge} which manages and contains this connection.
	 * @return the parent Edge
	 */
	public Edge getParent() {
		return parent;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}

	@Override
	public String getComment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setComment(String comment) {
		// TODO Auto-generated method stub
		
	}
}
