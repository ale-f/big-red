package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dk.itu.big_red.model.assistants.IPropertyProvider;
import dk.itu.big_red.model.assistants.PropertyScratchpad;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.interfaces.ILink;

/**
 * A Link is the superclass of {@link Edge}s and {@link OuterName}s &mdash;
 * model objects which have multiple connections to {@link Point}s.
 * @author alec
 * @see ILink
 */
public abstract class Link extends Layoutable implements ILink {
	/**
	 * The property name fired when a point is added or removed.
	 */
	@RedProperty(fired = Point.class, retrieved = List.class)
	public static final String PROPERTY_POINT = "LinkPoint";
	
	/**
	 * <strong>Connection</strong>s are fake, transient model objects, created
	 * on demand by {@link Link}s. They represent a single {@link
	 * org.eclipse.draw2d.Connection} on the bigraph, joining a {@link Link} to
	 * a {@link Point}.
	 * @author alec
	 *
	 */
	public class Connection {
		private Point point;
		
		private Connection(Point point) {
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
			return Link.this;
		}
	}
	
	/**
	 * The {@link Point}s connected to this Link on the bigraph.
	 */
	private ArrayList<Point> points =
		new ArrayList<Point>();
	
	public Link() {
	}
	
	/**
	 * Adds the given {@link Point} to this Link's set of points.
	 * @param point a Point
	 */
	protected void addPoint(Point point) {
		if (point == null)
			return;
		points.add(point);
		point.setLink(this);
		firePropertyChange(PROPERTY_POINT, null, point);
	}
	
	public void addPoint(PropertyScratchpad context, Point point) {
		context.<Point>getModifiableList(
				this, Link.PROPERTY_POINT, getPoints()).add(point);
		context.setProperty(point, Point.PROPERTY_LINK, this);
	}
	
	/**
	 * Removes the given {@link Point} from this Link's set of points.
	 * @param point a Point
	 */
	protected void removePoint(Point point) {
		if (points.remove(point)) {
			connections.remove(point);
			point.setLink(null);
			firePropertyChange(PROPERTY_POINT, point, null);
		}
	}
	
	public void removePoint(PropertyScratchpad context, Point point) {
		context.<Point>getModifiableList(
				this, Link.PROPERTY_POINT, getPoints()).remove(point);
		context.setProperty(point, Point.PROPERTY_LINK, null);
	}
	
	@Override
	public List<Point> getPoints() {
		return points;
	}
	
	@SuppressWarnings("unchecked")
	public List<Point> getPoints(IPropertyProvider context) {
		return (List<Point>)getProperty(context, PROPERTY_POINT);
	}
	
	private HashMap<Point, Link.Connection> connections =
		new HashMap<Point, Link.Connection>();
	
	/**
	 * Lazily creates and returns a {@link Link.Connection} connecting the
	 * given {@link Point} to this Link.
	 * @param p a {@link Point}
	 * @return a {@link Link.Connection}, which could go away at any point
	 */
	public Link.Connection getConnectionFor(Point p) {
		if (!points.contains(p))
			return null;
		Link.Connection l = connections.get(p);
		if (l == null)
			connections.put(p, l = new Link.Connection(p));
		return l;
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
	protected Object getProperty(String name) {
		if (PROPERTY_POINT.equals(name)) {
			return getPoints();
		} else return super.getProperty(name);
	}
}
