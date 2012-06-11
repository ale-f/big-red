package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.assistants.IPropertyProvider;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;

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
	 * The {@link Point}s connected to this Link on the bigraph.
	 */
	private ArrayList<Point> points = new ArrayList<Point>();
	
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
