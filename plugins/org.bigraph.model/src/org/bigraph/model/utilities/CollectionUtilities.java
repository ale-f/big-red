package org.bigraph.model.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class CollectionUtilities {
	private CollectionUtilities() {}
	
	public static final <T> List<T> collect(Iterable<? extends T> it) {
		return collect(it.iterator());
	}
	
	public static final <T> List<T> collect(Iterator<? extends T> it) {
		ArrayList<T> r = new ArrayList<T>();
		while (it.hasNext())
			r.add(it.next());
		return Collections.unmodifiableList(r);
	}
	
	public static final <T> List<T> collect(
			Iterable<? extends T> it, Comparator<? super T> cmp) {
		return collect(it.iterator(), cmp);
	}
	
	public static final <T> List<T> collect(
			Iterator<? extends T> it, Comparator<? super T> cmp) {
		ArrayList<T> r = new ArrayList<T>();
		while (it.hasNext()) {
			T i = it.next();
			int position = Collections.binarySearch(r, i, cmp);
			if (position < 0)
				position = -(position + 1);
			r.add(position, i);
		}
		return Collections.unmodifiableList(r);
	}
}
