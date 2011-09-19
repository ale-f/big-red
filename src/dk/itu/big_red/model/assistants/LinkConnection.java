package dk.itu.big_red.model.assistants;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.Connection;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Point;

/**
 * An LinkConnection is a fake model object, created on demand by {@link
 * Link}s, which corresponds to an Eclipse {@link Connection}. They represent
 * single {@link Connection}s on the bigraph, joining a {@link Link} to a
 * {@link Point}.
 * @author alec
 *
 */
public class LinkConnection extends ModelObject implements IAdaptable {
	private Point point;
	private Link link;
	
	public LinkConnection(Link link, Point point) {
		this.link = link;
		this.point = point;
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
