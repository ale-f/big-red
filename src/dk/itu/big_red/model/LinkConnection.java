package dk.itu.big_red.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.Connection;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.assistants.ModelPropertySource;

/**
 * An LinkConnection is the model object behind an actual {@link Connection}
 * on the bigraph. {@link Link}s create and manage them.
 * @author alec
 *
 */
public class LinkConnection extends ModelObject implements IAdaptable {
	/**
	 * The property name fired when this connection's {@link Point} changes.
	 */
	public static final String PROPERTY_POINT = "LinkConnectionPoint";
	/**
	 * The property name fired when this connection's {@link Link} changes.
	 */
	public static final String PROPERTY_LINK = "LinkConnectionLink";
	
	private Point point;
	private Link link;
	
	public LinkConnection(Link link) {
		this.link = link;
	}
	
	/**
	 * Sets the {@link Point} at one end of this connection.
	 * @param point the new Point
	 */
	public void setPoint(Point point) {
		if (point != null) {
			Point oldPoint = this.point;
			this.point = point;
			listeners.firePropertyChange(PROPERTY_POINT, oldPoint, point);
		}
	}
	
	/**
	 * Gets the {@link Point} at one end of this connection.
	 * @return the current Point
	 */
	public Point getPoint() {
		return point;
	}
	
	/**
	 * Gets the {@link Link} which manages and contains this connection.
	 * @return the current Link
	 */
	public Link getLink() {
		return link;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return new ModelPropertySource(getLink());
		} else return null;
	}
}
