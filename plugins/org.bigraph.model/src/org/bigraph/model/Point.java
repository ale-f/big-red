package org.bigraph.model;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
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
	
	public final class ChangeConnect extends PointChange {
		public final Link link;
		
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
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.<Point>getModifiableList(
					link, Link.PROPERTY_POINT, link.getPoints()).
				add(getCreator());
			context.setProperty(getCreator(), Point.PROPERTY_LINK, link);
		}
	}
	
	public final class ChangeDisconnect extends PointChange {
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
		
		@Override
		public void simulate(PropertyScratchpad context) {
			Link l = getCreator().getLink(context);
			
			context.<Point>getModifiableList(
					l, Link.PROPERTY_POINT, l.getPoints()).
				remove(getCreator());
			context.setProperty(getCreator(), Point.PROPERTY_LINK, null);
		}
	}

	static {
		ExecutorManager.getInstance().addHandler(new PointHandler());
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
	
	public IChange changeConnect(Link l) {
		return new ChangeConnect(l);
	}
	
	public IChange changeDisconnect() {
		return new ChangeDisconnect();
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
	
	public static class ChangeConnectDescriptor implements IChangeDescriptor {
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
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			Point p = point.lookup(context, r);
			if (p == null)
				throw new ChangeCreationException(this,
						"" + point + " didn't resolve to a Point");
			Link l = link.lookup(context, r);
			if (l == null)
				throw new ChangeCreationException(this,
						"" + link + " didn't resolve to a Link");
			return p.changeConnect(l);
		}
		
		@Override
		public String toString() {
			return "ChangeDescriptor(connect " + point + " to " + link + ")";
		}
	}
	
	public static class ChangeDisconnectDescriptor implements IChangeDescriptor {
		private final Identifier point;
		private final Link.Identifier link;
		
		public ChangeDisconnectDescriptor(Identifier point) {
			this(point, null);
		}
		
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
			return safeClassCmp(this, obj_) &&
					safeEquals(getPoint(),
							((ChangeDisconnectDescriptor)obj_).getPoint());
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(
					ChangeDisconnectDescriptor.class, point);
		}
		
		@Override
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			Point p = point.lookup(context, r);
			if (p == null)
				throw new ChangeCreationException(this,
						"" + point + " didn't resolve to a Point");
			return p.changeDisconnect();
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
