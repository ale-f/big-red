package dk.itu.big_red.model.assistants;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;

/**
 * <q>Pay no attention to the man behind the curtain!</q>
 * @author alec
 */
public interface IPropertyProviders {
	public interface IPropertyProviderProxy {
		public IPropertyProvider getProvider(IPropertyProvider o);
	}
	
	public interface IPropertyProvider {
		public Object getProperty(String name);
	}
	
	public interface IModelObjectPropertyProvider extends IPropertyProvider {
		public String getComment();
	}
	
	public interface IColourablePropertyProvider extends IModelObjectPropertyProvider {
		public ReadonlyColour getFillColour();
		public ReadonlyColour getOutlineColour();
	}
	
	public interface ILayoutablePropertyProvider extends IColourablePropertyProvider {
		public String getName();
		public Rectangle getLayout();
		public Container getParent();
	}
	
	public interface IContainerPropertyProvider extends ILayoutablePropertyProvider {
		public List<Layoutable> getChildren();
	}
	
	public interface IPointPropertyProvider extends ILayoutablePropertyProvider {
		public Link getLink();
	}
	
	public interface ILinkPropertyProvider extends ILayoutablePropertyProvider {
		public List<Point> getPoints();
	}
	
	public interface ISitePropertyProvider extends ILayoutablePropertyProvider {
		public String getAlias();
	}
	
	public interface INodePropertyProvider extends ILayoutablePropertyProvider {
		public String getParameter();
	}
}
