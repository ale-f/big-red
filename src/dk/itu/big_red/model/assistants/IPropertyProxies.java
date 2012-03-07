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
public interface IPropertyProxies {
	public interface IModelObjectPropertyProxy {
		public String getComment();
	}
	
	public interface IColourablePropertyProxy extends IModelObjectPropertyProxy {
		public ReadonlyColour getFillColour();
		public ReadonlyColour getOutlineColour();
	}
	
	public interface ILayoutablePropertyProxy extends IColourablePropertyProxy {
		public String getName();
		public Rectangle getLayout();
		public Container getParent();
	}
	
	public interface IContainerPropertyProxy extends ILayoutablePropertyProxy {
		public List<Layoutable> getChildren();
	}
	
	public interface IPointPropertyProxy extends ILayoutablePropertyProxy {
		public Link getLink();
	}
	
	public interface ILinkPropertyProxy extends ILayoutablePropertyProxy {
		public List<Point> getPoints();
	}
}
