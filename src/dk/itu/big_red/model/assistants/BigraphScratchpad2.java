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
	
	private String getKey(IPropertyProvider m, String property) {
		return "!" + System.identityHashCode(m) + "!" + property + "!";
	}
	
	public void setValue(IPropertyProvider m, String property, Object newValue) {
		changes.put(getKey(m, property), newValue);
	}
	
	protected Object getValue(IPropertyProvider m, String property) {
		return changes.get(getKey(m, property));
	}
	
	public boolean hasValue(IPropertyProvider m, String property) {
		return changes.containsKey(getKey(m, property));
	}
	
	public BigraphScratchpad2 clear() {
		changes.clear();
		return this;
	}
	
	public final class ModelObjectProxy implements
	IModelObjectPropertyProvider, IColourablePropertyProvider,
	ILayoutablePropertyProvider, IContainerPropertyProvider,
	IPointPropertyProvider, ILinkPropertyProvider, ISitePropertyProvider,
	INodePropertyProvider {
		private IPropertyProvider object;
		
		public ModelObjectProxy(IPropertyProvider object) {
			this.object = object;
		}
		
		protected Object get(String property) {
			return (hasValue(object, property) ? getValue(object, property) :
				object.getProperty(property));
		}
		
		@Override
		public String getComment() {
			return (String)get(ModelObject.PROPERTY_COMMENT);
		}
		
		@Override
		public ReadonlyColour getFillColour() {
			return (Colour)get(Colourable.PROPERTY_FILL);
		}

		@Override
		public ReadonlyColour getOutlineColour() {
			return (Colour)get(Colourable.PROPERTY_OUTLINE);
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

		@SuppressWarnings("unchecked") @Override
		public List<Layoutable> getChildren() {
			return (List<Layoutable>)get(Container.PROPERTY_CHILD);
		}

		@Override
		public Link getLink() {
			return (Link)get(Point.PROPERTY_LINK);
		}

		@SuppressWarnings("unchecked") @Override
		public List<Point> getPoints() {
			return (List<Point>)get(Link.PROPERTY_POINT);
		}
		
		@Override
		public String getAlias() {
			return (String)get(Site.PROPERTY_ALIAS);
		}
		
		@Override
		public String getParameter() {
			return (String)get(Node.PROPERTY_PARAMETER);
		}

		@Override
		public Object getProperty(String name) {
			return get(name);
		}
	}
	
	@Override
	public IPropertyProvider getProvider(IPropertyProvider o) {
		return new ModelObjectProxy(o);
	}
}
