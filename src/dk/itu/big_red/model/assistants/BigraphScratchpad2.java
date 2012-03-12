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
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.IPropertyProviders.IColourablePropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IContainerPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.ILayoutablePropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.ILinkPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IModelObjectPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.INodePropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IPointPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IPropertyProvider;
import dk.itu.big_red.model.assistants.IPropertyProviders.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.IPropertyProviders.ISitePropertyProvider;

public class BigraphScratchpad2 implements IPropertyProviderProxy {
	private Map<String, Object> changes = new HashMap<String, Object>();
	
	private String getKey(ModelObject m, String property) {
		return "!" + System.identityHashCode(m) + "!" + property + "!";
	}
	
	public void setValue(ModelObject m, String property, Object newValue) {
		changes.put(getKey(m, property), newValue);
	}
	
	protected Object getValue(ModelObject m, String property) {
		return changes.get(getKey(m, property));
	}
	
	public boolean hasValue(ModelObject m, String property) {
		return changes.containsKey(getKey(m, property));
	}
	
	public BigraphScratchpad2 clear() {
		changes.clear();
		return this;
	}
	
	public class ModelObjectProxy implements IModelObjectPropertyProvider {
		private ModelObject object;
		
		public ModelObjectProxy(ModelObject object) {
			this.object = object;
		}
		
		protected ModelObject getObject() {
			return object;
		}
		
		protected boolean should(String property) {
			return hasValue(object, property);
		}
		
		protected Object get(String property) {
			return (should(property) ? getValue(object, property) :
				getObject().getProperty(property));
		}
		
		@Override
		public String getComment() {
			return (String)get(ModelObject.PROPERTY_COMMENT);
		}
	}
	
	public class ColourableProxy extends ModelObjectProxy
		implements IColourablePropertyProvider {
		public ColourableProxy(Colourable object) {
			super(object);
		}
		
		@Override
		public ReadonlyColour getFillColour() {
			return (Colour)get(Colourable.PROPERTY_FILL);
		}

		@Override
		public ReadonlyColour getOutlineColour() {
			return (Colour)get(Colourable.PROPERTY_OUTLINE);
		}
	}
	
	public class LayoutableProxy extends ColourableProxy
		implements ILayoutablePropertyProvider {
		public LayoutableProxy(Layoutable object) {
			super(object);
		}

		@Override
		public String getName() {
			return (String)get(Layoutable.PROPERTY_NAME);
		}

		@Override
		public Rectangle getLayout() {
			return (Rectangle)get(Layoutable.PROPERTY_LAYOUT);
		}

		@Override
		public Container getParent() {
			return (Container)get(Layoutable.PROPERTY_PARENT);
		}
	}
	
	public class ContainerProxy extends LayoutableProxy
		implements IContainerPropertyProvider {
		public ContainerProxy(Container object) {
			super(object);
		}

		@SuppressWarnings("unchecked") @Override
		public List<Layoutable> getChildren() {
			return (List<Layoutable>)get(Container.PROPERTY_CHILD);
		}
	}
	
	public class PointProxy extends LayoutableProxy
		implements IPointPropertyProvider {
		public PointProxy(Point object) {
			super(object);
		}

		@Override
		public Link getLink() {
			return (Link)get(Point.PROPERTY_LINK);
		}
	}
	
	public class LinkProxy extends LayoutableProxy
		implements ILinkPropertyProvider {
		public LinkProxy(Link object) {
			super(object);
		}

		@SuppressWarnings("unchecked") @Override
		public List<Point> getPoints() {
			return (List<Point>)get(Link.PROPERTY_POINT);
		}
	}
	
	public class SiteProxy extends LayoutableProxy
		implements ISitePropertyProvider {
		public SiteProxy(Site object) {
			super(object);
		}
		
		@Override
		public String getAlias() {
			return (String)get(Site.PROPERTY_ALIAS);
		}
	}

	public class NodeProxy extends ContainerProxy
		implements INodePropertyProvider {
		public NodeProxy(Node object) {
			super(object);
		}
		
		@Override
		public String getParameter() {
			return (String)get(Node.PROPERTY_PARAMETER);
		}
	}
	
	@Override
	public IPropertyProvider getProvider(IPropertyProvider o) {
		if (o instanceof Node) {
			return new NodeProxy((Node)o);
		} else if (o instanceof Site) {
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
		} else if (o instanceof ModelObject) {
			return new ModelObjectProxy((ModelObject)o);
		} else return o;
	}
}
