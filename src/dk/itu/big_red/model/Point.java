package dk.itu.big_red.model;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.IPoint;
import dk.itu.big_red.util.Colour;
import dk.itu.big_red.util.ReadonlyColour;

/**
 * Points are objects which can be connected to <em>at most one</em> {@link
 * Link} - {@link Port}s and {@link InnerName}s.
 * @author alec
 * @see IPoint
 */
public abstract class Point extends Layoutable implements IPoint {
	public class ChangeConnect extends Change {
		public Point point;
		public Link link;
		
		public ChangeConnect(Point point, Link link) {
			this.point = point;
			this.link = link;
		}

		@Override
		public Change inverse() {
			return new ChangeDisconnect(point, link);
		}
		
		@Override
		public boolean isReady() {
			return (point != null && link != null);
		}
		
		@Override
		public String toString() {
			return "Change(connect " + point + " to " + link + ")";
		}
	}
	
	public class ChangeDisconnect extends Change {
		public Point point;
		public Link link;
		
		public ChangeDisconnect(Point point, Link link) {
			this.point = point;
			this.link = link;
		}

		@Override
		public Change inverse() {
			return new ChangeConnect(point, link);
		}
		
		@Override
		public boolean isReady() {
			return (point != null && link != null);
		}
		
		@Override
		public String toString() {
			return "Change(disconnect " + point + " from " + link + ")";
		}
	}
	
	/**
	 * The property name fired when the source edge changes.
	 */
	public static final String PROPERTY_LINK = "PointLink";
	
	/**
	 * The colour to be given to Points not connected to a {@link Link}.
	 */
	public static final ReadonlyColour DEFAULT_COLOUR = new Colour("red");
	
	@Override
	public ILink getILink() {
		return link;
	}

	protected Link link = null;
	
	/**
	 * Replaces the current {@link Link} of this Point.
	 * @param l the new {@link Link}
	 * @return the previous {@link Link}, or <code>null</code> if
	 * there wasn't one
	 */
	public Link setLink(Link l) {
		Link oldLink = link;
		link = l;
		firePropertyChange(Point.PROPERTY_LINK, oldLink, l);
		return oldLink;
	}
	
	public Link getLink() {
		return link;
	}
	
	public Change changeConnect(Link l) {
		return new ChangeConnect(this, l);
	}
	
	public Change changeDisconnect(Link l) {
		return new ChangeDisconnect(this, l);
	}
}
