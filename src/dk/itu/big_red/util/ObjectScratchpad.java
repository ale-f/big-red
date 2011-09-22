package dk.itu.big_red.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;

/**
 * The ObjectScratchpad is a wrapper around a {@link Map Map<Object, Object>}
 * which maps objects to copies of themselves, allowing the copies to be
 * modified without affecting the original.
 * @author alec
 *
 */
public class ObjectScratchpad {
	private HashMap<LayoutableModelObject, Rectangle> layouts =
			new HashMap<LayoutableModelObject, Rectangle>();
	
	private HashMap<LayoutableModelObject, Container> parents =
			new HashMap<LayoutableModelObject, Container>();
	
	public void clear() {
		layouts.clear();
		parents.clear();
	}
	
	public Rectangle getLayoutFor(LayoutableModelObject a) {
		Rectangle b = layouts.get(a);
		if (b == null) {
			b = a.getLayout();
			layouts.put(a, b);
		}
		return b;
	}
	
	public void setLayoutFor(LayoutableModelObject a, Rectangle b) {
		layouts.put(a, b);
	}
	
	public LayoutableModelObject getParentFor(LayoutableModelObject a) {
		Container b = parents.get(a);
		if (b == null) {
			b = a.getParent();
			parents.put(a, b);
		}
		return b;
	}
	
	public void setParentFor(LayoutableModelObject a, Container b) {
		parents.put(a, b);
	}
}
