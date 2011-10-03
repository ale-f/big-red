package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.graphics.RGB;

import dk.itu.big_red.editors.bigraph.parts.PortPart;
import dk.itu.big_red.model.assistants.LinkConnection;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.IPoint;
import dk.itu.big_red.model.interfaces.internal.IOutlineColourable;
import dk.itu.big_red.util.Utility;

/**
 * A Link is the superclass of {@link Edge}s and {@link OuterName}s &mdash;
 * model objects which have multiple {@link LinkConnection}s to {@link Point}s.
 * @author alec
 * @see ILink
 */
public abstract class Link extends Layoutable implements IOutlineColourable, ILink {
	/**
	 * The property name fired when a point is added to, or removed from, this
	 * Link.
	 */
	public static final String PROPERTY_POINT = "LinkPoint";
	
	/**
	 * The {@link Point}s connected to this Link on the bigraph.
	 */
	private ArrayList<Point> points =
		new ArrayList<Point>();
	
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
	
	private HashMap<Point, LinkConnection> connections =
		new HashMap<Point, LinkConnection>();
	
	/**
	 * Lazily creates and returns a {@link LinkConnection} connecting the given
	 * {@link Point} to this Link.
	 * 
	 * <p><strong>Do not call this function</strong>; it's intended only for
	 * the use of {@link PortPart}s and {@link LinkPart}s.
	 * @param p a {@link Point}
	 * @return a {@link LinkConnection}, which could go away at any point
	 */
	public LinkConnection getConnectionFor(Point p) {
		if (!points.contains(p))
			return null;
		LinkConnection l = connections.get(p);
		if (l == null) {
			l = new LinkConnection(this, p);
			connections.put(p, l);
		}
		return l;
	}
	
	private RGB outlineColour = new RGB(0, 127, 0);
	
	@Override
	public void setOutlineColour(RGB outlineColour) {
		RGB oldColour = getOutlineColour();
		this.outlineColour = outlineColour;
		firePropertyChange(PROPERTY_OUTLINE_COLOUR, oldColour, outlineColour);
	}

	@Override
	public RGB getOutlineColour() {
		return outlineColour;
	}
	
	@Override
	public Iterable<IPoint> getIPoints() {
		return Utility.only(points, IPoint.class);
	}
}
