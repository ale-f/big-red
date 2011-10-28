package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.editors.bigraph.parts.PortPart;
import dk.itu.big_red.model.assistants.ModelPropertySource;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.util.Colour;

/**
 * A Link is the superclass of {@link Edge}s and {@link OuterName}s &mdash;
 * model objects which have multiple connections to {@link Point}s.
 * @author alec
 * @see ILink
 */
public abstract class Link extends Layoutable implements ILink {
	/**
	 * <strong>Connection</strong>s are fake, transient model objects, created
	 * on demand by {@link Link}s. They represent a single {@link
	 * org.eclipse.draw2d.Connection} on the bigraph, joining a {@link Link} to
	 * a {@link Point}.
	 * @author alec
	 *
	 */
	public class Connection extends ModelObject implements IAdaptable {
		private Point point;
		private Link link;
		
		private Connection(Link link, Point point) {
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
	
	/**
	 * The property name fired when a point is added to, or removed from, this
	 * Link. The property values are {@link Point}s.
	 */
	public static final String PROPERTY_POINT = "LinkPoint";
	
	/**
	 * The {@link Point}s connected to this Link on the bigraph.
	 */
	private ArrayList<Point> points =
		new ArrayList<Point>();
	
	public Link() {
		setOutlineColour(new Colour("green"));
	}
	
	/**
	 * Adds the given {@link Point} to this Link's set of points.
	 * @param point a Point
	 */
	public void addPoint(Point point) {
		if (point == null)
			return;
		points.add(point);
		point.setLink(this);
		firePropertyChange(Link.PROPERTY_POINT, null, point);
	}
	
	/**
	 * Removes the given {@link Point} from this Link's set of points.
	 * @param point a Point
	 */
	public void removePoint(Point point) {
		if (points.remove(point)) {
			connections.remove(point);
			point.setLink(null);
			firePropertyChange(Link.PROPERTY_POINT, point, null);
		}
	}
	
	public List<Point> getPoints() {
		return points;
	}
	
	private HashMap<Point, Link.Connection> connections =
		new HashMap<Point, Link.Connection>();
	
	/**
	 * Lazily creates and returns a {@link Link.Connection} connecting the
	 * given {@link Point} to this Link.
	 * 
	 * <p><strong>Do not call this function</strong>; it's intended only for
	 * the use of {@link PortPart}s and {@link LinkPart}s.
	 * @param p a {@link Point}
	 * @return a {@link Link.Connection}, which could go away at any point
	 */
	public Link.Connection getConnectionFor(Point p) {
		if (!points.contains(p))
			return null;
		Link.Connection l = connections.get(p);
		if (l == null) {
			l = new Link.Connection(this, p);
			connections.put(p, l);
		}
		return l;
	}
	
	@Override
	public Iterable<Point> getIPoints() {
		return points;
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Special notes for {@link Link}:</strong>
	 * <ul>
	 * <li>Passing {@link #PROPERTY_POINT} will return a {@link List}&lt;{@link
	 * Point}&gt;, <strong>not</strong> a {@link Point}.
	 * </ul>
	 */
	@Override
	public Object getProperty(String name) {
		if (name.equals(PROPERTY_POINT)) {
			return getPoints();
		} else return super.getProperty(name);
	}
}
