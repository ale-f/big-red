package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.interfaces.IPoint;

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
	}
	
	public class ChangeConnect extends PointChange {
		public Link link;
		public ChangeConnect(Link link) {
			this.link = link;
		}

		@Override
		public boolean isReady() {
			return (link != null);
		}
		
		@Override
		public ChangeDisconnect inverse() {
			return new ChangeDisconnect();
		}
		
		@Override
		public String toString() {
			return "Change(connect " + getCreator() + " to " + link + ")";
		}
	}
	
	public class ChangeDisconnect extends PointChange {
		private Link oldLink;
		@Override
		public void beforeApply() {
			oldLink = getCreator().getLink();
		}
		
		@Override
		public boolean canInvert() {
			return (oldLink != null);
		}
		
		@Override
		public ChangeConnect inverse() {
			return new ChangeConnect(oldLink);
		}
		
		@Override
		public String toString() {
			return "Change(disconnect " + getCreator() + ")";
		}
	}

	private Link link = null;
	
	/**
	 * Replaces the current {@link Link} of this Point.
	 * @param l the new {@link Link}
	 * @return the previous {@link Link}, or <code>null</code> if
	 * there wasn't one
	 */
	void setLink(Link l) {
		Link oldLink = link;
		link = l;
		firePropertyChange(PROPERTY_LINK, oldLink, l);
	}
	
	@Override
	public Link getLink() {
		return link;
	}
	
	public Link getLink(PropertyScratchpad context) {
		return (Link)getProperty(context, PROPERTY_LINK);
	}
	
	public LayoutableChange changeConnect(Link l) {
		return new ChangeConnect(l);
	}
	
	public LayoutableChange changeDisconnect() {
		return new ChangeDisconnect();
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_LINK.equals(name)) {
			return getLink();
		} else return super.getProperty(name);
	}
}