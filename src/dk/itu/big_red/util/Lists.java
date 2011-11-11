package dk.itu.big_red.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Utility methods for manipulating and creating {@link List}s and {@link
 * Collection}s.
 * @author alec
 *
 */
public final class Lists {
	/**
	 * Returns a <i>class-grouped</i> copy of the given {@link Collection};
	 * elements of any of the {@link Class}es passed as varargs will be grouped
	 * together. (All other elements will be omitted by default; to put them at
	 * the end of the list, pass {@link Object Object.class} as the last
	 * vararg.)
	 * @param list a {@link Collection}
	 * @param classes an array of {@link Class Class&lt;? extends T&gt;}
	 * @return a class-grouped {@link ArrayList}
	 */
	public static <T> ArrayList<T>
	group(Collection<? extends T> list, Object... classes) {
		ArrayList<T> r = new ArrayList<T>(),
				working = copy(list);
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
	
	/**
	 * Returns a subset of the given {@link Collection} which contains only
	 * those objects of the given {@link Class}.
	 * @param list a {@link Collection}
	 * @param klass the {@link Class} to filter by
	 * @return a {@link List} of objects of the given {@link Class}
	 */
	@SuppressWarnings("unchecked")
	public static <T, V>
	ArrayList<V> only(Collection<? extends T> list, Class<V> klass) {
		ArrayList<V> r = new ArrayList<V>();
		for (T i : list)
			if (klass.isInstance(i))
				r.add((V)i);
		return r;
	}
	
	/**
	 * Copies a {@link Collection} into a new {@link ArrayList}.
	 * @param c a {@link Collection}
	 * @return a new {@link ArrayList}
	 */
	public static <T> ArrayList<T> copy(Collection<? extends T> c) {
		return new ArrayList<T>(c);
	}
}
