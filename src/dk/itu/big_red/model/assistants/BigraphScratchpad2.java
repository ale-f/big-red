package dk.itu.big_red.model.assistants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.IPropertyProviders.IColourablePropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IContainerPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.ILayoutablePropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.ILinkPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IModelObjectPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IPointPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.IPropertyProviders.ISitePropertyProvider;

public class BigraphScratchpad2 implements IPropertyProviderProxy {
	static class MaybeNull<T> {
		private T value;
		public MaybeNull(T value) {
			set(value);
		}
		
		public T get() {
			return value;
		}
		
		public void set(T value) {
			this.value = value;
		}
	}
	
	@Override
	public IPropertyProvider getProvider(IPropertyProvider o) {
		if (o instanceof ModelObject) {
			IPropertyProvider p = proxies.get(o);
			if (p != null)
				return p;
		}
		return o;
	}
	
	private ModelObjectProxy createProvider(ModelObject o) {
		if (o instanceof Site) {
			return new SiteProxy((Site)o);
		} else if (o instanceof Link) {
			return new LinkProxy((Link)o);
		} else if (o instanceof Point) {
			return new PointProxy((Point)o);
		} else if (o instanceof Container) {
			return new ContainerProxy((Container)o);
		} else if (o instanceof Layoutable) {
			return new LayoutableProxy((Layoutable)o);
		} else if (o instanceof Colourable) {
			return new ColourableProxy((Colourable)o);
		} else return new ModelObjectProxy(o);
	}
	
	private Map<ModelObject, IPropertyProvider> proxies =
			new HashMap<ModelObject, IPropertyProvider>();
	
	public BigraphScratchpad2 clear() {
		proxies.clear();
		return this;
	}
	
	public IPropertyProvider requireProvider(ModelObject o) {
		IPropertyProvider p = proxies.get(o);
		if (p == null)
			proxies.put(o, p = createProvider(o));
		return p;
	}
	
	public class ModelObjectProxy implements IModelObjectPropertyProvider {
		MaybeNull<String> comment;
		private ModelObject object;
		
		public ModelObjectProxy(ModelObject object) {
			this.object = object;
		}
		
		protected ModelObject getObject() {
			return object;
		}
		
		@Override
		public String getComment() {
			return (comment != null ?
					comment.get() : getObject().getComment());
		}
	}
	
	public class ColourableProxy extends ModelObjectProxy
		implements IColourablePropertyProvider {
		MaybeNull<ReadonlyColour> fill, outline;
		public ColourableProxy(Colourable object) {
			super(object);
		}
		
		@Override
		public ReadonlyColour getFillColour() {
			return (fill != null ?
					fill.get() : ((Colourable)getObject()).getFillColour());
		}

		@Override
		public ReadonlyColour getOutlineColour() {
			return (outline != null ?
					outline.get() : ((Colourable)getObject()).getOutlineColour());
		}
	}
	
	public class LayoutableProxy extends ColourableProxy
		implements ILayoutablePropertyProvider {
		MaybeNull<String> name;
		MaybeNull<Rectangle> layout;
		MaybeNull<Container> parent;
		
		public LayoutableProxy(Layoutable object) {
			super(object);
		}

		@Override
		public String getName() {
			return (name != null ?
					name.get() : ((Layoutable)getObject()).getName());
		}

		@Override
		public Rectangle getLayout() {
			return (layout != null ?
					layout.get() : ((Layoutable)getObject()).getLayout());
		}

		@Override
		public Container getParent() {
			return (parent != null ?
					parent.get() : ((Layoutable)getObject()).getParent());
		}
	}
	
	public class ContainerProxy extends LayoutableProxy
		implements IContainerPropertyProvider {
		MaybeNull<List<Layoutable>> children;
		
		public ContainerProxy(Container object) {
			super(object);
		}

		@Override
		public List<Layoutable> getChildren() {
			return (children != null ?
					children.get() : ((Container)getObject()).getChildren());
		}
	}
	
	public class PointProxy extends LayoutableProxy
		implements IPointPropertyProvider {
		MaybeNull<Link> link;
		
		public PointProxy(Point object) {
			super(object);
		}

		@Override
		public Link getLink() {
			return (link != null ?
					link.get() : ((Point)getObject()).getLink());
		}
	}
	
	public class LinkProxy extends LayoutableProxy
		implements ILinkPropertyProvider {
		MaybeNull<List<Point>> points;
		
		public LinkProxy(Link object) {
			super(object);
		}

		@Override
		public List<Point> getPoints() {
			return (points != null ?
					points.get() : ((Link)getObject()).getPoints());
		}
	}
	
	public class SiteProxy extends LayoutableProxy
		implements ISitePropertyProvider {
		MaybeNull<String> alias;
		
		public SiteProxy(Site object) {
			super(object);
		}
		
		@Override
		public String getAlias() {
			return (alias != null ?
					alias.get() : ((Site)getObject()).getAlias());
		}
	}
}
