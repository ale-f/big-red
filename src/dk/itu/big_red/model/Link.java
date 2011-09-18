package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.RGB;
import dk.itu.big_red.model.assistants.NamespaceManager;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.IPoint;
import dk.itu.big_red.model.interfaces.internal.INameable;
import dk.itu.big_red.model.interfaces.internal.IOutlineColourable;
import dk.itu.big_red.part.InnerNamePart;
import dk.itu.big_red.part.PortPart;

/**
 * A Link is the superclass of {@link Edge}s and {@link OuterName}s &mdash;
 * model objects which have multiple {@link LinkConnection}s to {@link Point}s.
 * @author alec
 * @see ILink
 */
public abstract class Link extends LayoutableModelObject implements IAdaptable, INameable, IOutlineColourable, ILink {
	/**
	 * The property name fired when the target edge set changes (that is, an
	 * edge for which this object is the target is added or removed).
	 */
	public static final String PROPERTY_TARGET_EDGE = "LinkTargetEdge";
	
	/**
	 * The {@link LinkConnection}s that comprise this Link on the bigraph.
	 */
	private ArrayList<LinkConnection> connections =
		new ArrayList<LinkConnection>();
	
	/**
	 * Adds the given {@link Point} to this Link's set of points, and creates a
	 * new {@link LinkConnection} joining it to this Link's {@link Link}.
	 * @param point a Point
	 */
	public void addPoint(Point point) {
		LinkConnection c = new LinkConnection(this);
		c.setPoint(point);
		addConnection(c);
		
		point.setLink(this);
	}
	
	/**
	 * Removes the given {@link Point} from this Link's set of points and
	 * destroys its {@link LinkConnection}.
	 * 
	 * @param point a Point
	 */
	public void removePoint(Point point) {
		for (LinkConnection e : connections) {
			if (e.getPoint() == point) {
				point.setLink(null);
				removeConnection(e);
				
				break;
			}
		}
	}
	
	private void addConnection(LinkConnection e) {
		connections.add(e);
		firePropertyChange(Link.PROPERTY_TARGET_EDGE, null, e);
	}

	private void removeConnection(LinkConnection e) {
		connections.remove(e);
		firePropertyChange(Link.PROPERTY_TARGET_EDGE, e, null);
	}
	
	public List<LinkConnection> getConnections() {
		return connections;
	}

	/**
	 * Returns the {@link LinkConnection} connecting the given {@link Point}
	 * to this Link.
	 * 
	 * <p><strong>Do not call this function</strong>; it's intended only for
	 * the use of {@link PortPart}s and {@link InnerNamePart}s.
	 * @param p a {@link Point}
	 * @return a {@link LinkConnection}, which could go away at any point
	 */
	public LinkConnection getConnectionFor(Point p) {
		for (LinkConnection l : connections)
			if (l.getPoint() == p)
				return l;
		return null;
	}
	
	@Override
	public String getName() {
		return getBigraph().getNamespaceManager().requireName(Link.class, this);
	}
	
	@Override
	public void setName(String name) {
		NamespaceManager nm = getBigraph().getNamespaceManager();
		String oldName = nm.getName(Link.class, this);
		if (name != null) {
			if (nm.setName(Link.class, this, name))
				firePropertyChange(PROPERTY_NAME, oldName, name);
		} else {
			String newName = nm.requireName(Link.class, this);
			if (!newName.equals(oldName))
				firePropertyChange(PROPERTY_NAME, oldName, newName);
		}
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
	public abstract Link clone();
	
	@Override
	public Iterable<IPoint> getIPoints() {
		ArrayList<IPoint> points = new ArrayList<IPoint>();
		for (LinkConnection c : connections)
			points.add(c.getPoint());
		return points;
	}
}
