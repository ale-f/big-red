package org.bigraph.model;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.interfaces.ILink;

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
	
	@Override
	public List<? extends Point> getPoints() {
		return points;
	}
	
	@SuppressWarnings("unchecked")
	public List<? extends Point> getPoints(PropertyScratchpad context) {
		return getProperty(context, PROPERTY_POINT, List.class);
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
	
	public static abstract class Identifier extends Layoutable.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (obj_ instanceof Identifier) {
				return getName().equals(((Identifier)obj_).getName());
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(Identifier.class, getName());
		}
		
		@Override
		public Link lookup(PropertyScratchpad context, Resolver r) {
			return require(r.lookup(context, this), Link.class);
		}
		
		@Override
		public abstract Identifier getRenamed(String name);
	}
	
	@Override
	public abstract Identifier getIdentifier();
	@Override
	public abstract Identifier getIdentifier(PropertyScratchpad context);
	
	@Override
	public void dispose() {
		if (points != null) {
			points.clear();
			points = null;
		}
		
		super.dispose();
	}
}
