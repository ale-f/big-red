package dk.itu.big_red.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Miscellaneous utility methods.
 * @author alec
 *
 */
public final class Utility {
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
