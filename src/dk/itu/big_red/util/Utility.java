package dk.itu.big_red.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.swt.graphics.RGB;

/**
 * Miscellaneous utility methods.
 * @author alec
 *
 */
public final class Utility {
	/**
	 * Converts a {@link RGB} colour to a string of the format
	 * <code>#rrggbb</code>.
	 * 
	 * @param c a RGB colour
	 * @return a string representation of the specified colour
	 */
	public static String colourToString(RGB c) {
		String r = Integer.toHexString(c.red);
		if (r.length() == 1)
			r = "0" + r;
		String g = Integer.toHexString(c.green);
		if (g.length() == 1)
			g = "0" + g;
		String b = Integer.toHexString(c.blue);
		if (b.length() == 1)
			b = "0" + b;
		return "#" + r + g + b;
	}
	
	/**
	 * Converts a string description of a colour to a {@link RGB} colour.
	 * 
	 * <p>At the moment, the string must be of the format <code>#rrggbb</code>,
	 * but this will become more lenient in future.
	 * @param c a string description of a colour
	 * @return a new RGB colour, or <code>null</code> if the string was invalid
	 */
	public static RGB colourFromString(String c) {
		if (c == null || c.length() != 7 || c.charAt(0) != '#')
			return new RGB(0, 0, 0);
		int r = Integer.parseInt(c.substring(1, 3), 16);
		int g = Integer.parseInt(c.substring(3, 5), 16);
		int b = Integer.parseInt(c.substring(5, 7), 16);
		return new RGB(r, g, b);
	}
	
	/**
	 * Returns a <i>class-grouped</i> copy of the given {@link List}; elements
	 * of any of the {@link Class}es passed as varargs will be grouped
	 * together. (All other elements will be omitted by default; to put them at
	 * the end of the list, pass {@link Object Object.class} as the last
	 * vararg.)
	 * @param list a List
	 * @param classes an array of {@link Class Class&lt;? extends T&gt;}
	 * @return a class-grouped {@link ArrayList}
	 */
	public static <T> ArrayList<T>
	groupListByClass(List<T> list, Object... classes) {
		ArrayList<T> r = new ArrayList<T>(),
				working = new ArrayList<T>(list);
		for (Object o : classes) {
			@SuppressWarnings("unchecked")
			Class<? extends T> c = (Class<? extends T>)o;
			Iterator<T> it = working.iterator();
			while (it.hasNext()) {
				T i = it.next();
				if (c.isInstance(i)) {
					r.add(i);
					it.remove();
				}
			}
		}
		return r;
	}
}
