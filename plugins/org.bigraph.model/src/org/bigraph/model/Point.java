package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
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
	
	abstract static class PointChangeDescriptor
			extends LayoutableChangeDescriptor {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(new PointDescriptorHandler());
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
		return getProperty(context, PROPERTY_LINK, Link.class);
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_LINK.equals(name)) {
			return getLink();
		} else return super.getProperty(name);
	}
	
	public static abstract class Identifier extends Layoutable.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public abstract Point lookup(PropertyScratchpad context, Resolver r);
		
		@Override
		public abstract Identifier getRenamed(String name);
	}
	
	@Override
	public abstract Identifier getIdentifier();
	@Override
	public abstract Identifier getIdentifier(PropertyScratchpad context);
	
	public static final class ChangeConnectDescriptor
			extends PointChangeDescriptor {
		private final Identifier point;
		private final Link.Identifier link;
		
		public ChangeConnectDescriptor(
				Identifier point, Link.Identifier link) {
			this.point = point;
			this.link = link;
		}
		
		public Identifier getPoint() {
			return point;
		}

		public Link.Identifier getLink() {
			return link;
		}

		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				ChangeConnectDescriptor obj = (ChangeConnectDescriptor)obj_;
				return
						safeEquals(getPoint(), obj.getPoint()) &&
						safeEquals(getLink(), obj.getLink());
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(
					ChangeConnectDescriptor.class, point, link);
		}
		
		@Override
		public ChangeDisconnectDescriptor inverse() {
			return new ChangeDisconnectDescriptor(getPoint(), getLink());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Point p = getPoint().lookup(context, r);
			Link l = getLink().lookup(context, r);
			context.<Point>getModifiableList(
					l, Link.PROPERTY_POINT, l.getPoints()).add(p);
			context.setProperty(p, Point.PROPERTY_LINK, l);
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(connect " + point + " to " + link + ")";
		}
	}
	
	public static final class ChangeDisconnectDescriptor
			extends PointChangeDescriptor {
		private final Identifier point;
		private final Link.Identifier link;
		
		public ChangeDisconnectDescriptor(
				Identifier point, Link.Identifier link) {
			this.point = point;
			this.link = link;
		}
		
		public Identifier getPoint() {
			return point;
		}
		
		public Link.Identifier getLink() {
			return link;
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				ChangeDisconnectDescriptor obj =
						(ChangeDisconnectDescriptor)obj_;
				return
						safeEquals(getPoint(), obj.getPoint()) &&
						safeEquals(getLink(), obj.getLink());
			} else return false;
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(
					ChangeDisconnectDescriptor.class, point, link);
		}
		
		@Override
		public ChangeConnectDescriptor inverse() {
			return new ChangeConnectDescriptor(getPoint(), getLink());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Point p = getPoint().lookup(context, r);
			Link l = getLink().lookup(context, r);
			
			context.<Point>getModifiableList(
					l, Link.PROPERTY_POINT, l.getPoints()).remove(p);
			context.setProperty(p, Point.PROPERTY_LINK, null);
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(disconnect " + point + ")";
		}
	}
	
	@Override
	public void dispose() {
		link = null;
		
		super.dispose();
	}
}
