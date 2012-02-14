package dk.itu.big_red.utilities;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility functions for manipulating DOM {@link Document}s and
 * {@link Element}s.
 * @author alec
 *
 */
public final class DOM {
	private DOM() {}
	
	/**
	 * Applies the specified name-value pairs to the specified element as
	 * attributes. (This uses {@link Element#setAttribute}, but is slightly
	 * less irritating, as it automatically converts names and values to
	 * strings.)
	 * @param d an Element
	 * @param attrs a vararg list of name-value pairs of any type
	 * @return d, for convenience
	 * @see #applyAttributesNS(Element, Object...)
	 */
	public static Element applyAttributes(Element d, Object... attrs) {
		for (int i = 0; i < attrs.length; i += 2)
			d.setAttribute(attrs[i].toString(), attrs[i + 1].toString());
		return d;
	}
}
