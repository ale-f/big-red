package dk.itu.big_red.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * The ObjectScratchpad is a wrapper around a {@link Map Map<Object, Object>}
 * which maps objects to copies of themselves, allowing the copies to be
 * modified without affecting the original.
 * @author alec
 *
 */
public class ObjectScratchpad {
	private HashMap<Object, Object> objects =
		new HashMap<Object, Object>();
	
	public void clear() {
		objects.clear();
	}
	
	public Rectangle getRectangle(Rectangle object) {
		Rectangle r = (Rectangle)objects.get(object);
		if (r == null) {
			r = object.getCopy();
			objects.put(object, r);
		}
		return r;
	}
}
