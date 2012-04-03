package dk.itu.big_red.model;

import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.ReadonlyColour;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.interfaces.IPoint;

/**
 * Points are objects which can be connected to <em>at most one</em> {@link
 * Link} - {@link Port}s and {@link InnerName}s.
 * @author alec
 * @see IPoint
 */
public abstract class Point extends Layoutable implements IPoint {
	/**
	 * The property name fired when the source edge changes.
	 */
	@RedProperty(fired = Link.class, retrieved = Link.class)
	public static final String PROPERTY_LINK = "PointLink";
	
	abstract class PointChange extends LayoutableChange {
		@Override
		public Point getCreator() {
			return Point.this;
		}
		
		public Link link;
		
		public PointChange(Link link) {
			this.link = link;
		}
		
		@Override
		public boolean isReady() {
			return (link != null);
		}
	}
	
	public class ChangeConnect extends PointChange {
		public ChangeConnect(Link link) {
			super(link);
		}

		@Override
		public ChangeDisconnect inverse() {
			return new ChangeDisconnect(link);
		}
		
		@Override
		public String toString() {
			return "Change(connect " + getCreator() + " to " + link + ")";
		}
	}
	
	public class ChangeDisconnect extends PointChange {
		public ChangeDisconnect(Link link) {
			super(link);
		}

		@Override
		public ChangeConnect inverse() {
			return new ChangeConnect(link);
		}
		
		@Override
		public String toString() {
			return "Change(disconnect " + getCreator() + " from " + link + ")";
		}
	}
	
	/**
	 * The colour to be given to Points not connected to a {@link Link}.
	 */
	public static final ReadonlyColour DEFAULT_COLOUR = new Colour("red");

	private Link link = null;
	
	/**
	 * Replaces the current {@link Link} of this Point.
	 * @param l the new {@link Link}
	 * @return the previous {@link Link}, or <code>null</code> if
	 * there wasn't one
	 */
	protected void setLink(Link l) {
		Link oldLink = link;
		link = l;
		firePropertyChange(Point.PROPERTY_LINK, oldLink, l);
	}
	
	@Override
	public Link getLink() {
		return link;
	}
	
	public Link getLink(IPropertyProviderProxy context) {
		return (Link)getProperty(context, PROPERTY_LINK);
	}
	
	public LayoutableChange changeConnect(Link l) {
		return new ChangeConnect(l);
	}
	
	public LayoutableChange changeDisconnect(Link l) {
		return new ChangeDisconnect(l);
	}
	
	@Override
	public Object getProperty(String name) {
		if (PROPERTY_LINK.equals(name)) {
			return getLink();
		} else return super.getProperty(name);
	}
}
