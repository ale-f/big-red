package org.bigraph.model.utilities.comparators;

import java.util.Comparator;

public abstract class ComparatorUtilities {
	private ComparatorUtilities() {}
	
	public interface Converter<T, V> {
		V convert(T object);
	}
	
	public static final <T, V> Comparator<T> convertComparator(
			final Converter<? super T, ? extends V> nr,
			final Comparator<? super V> cmp) {
		return new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return cmp.compare(nr.convert(o1), nr.convert(o2));
			}
		};
	}
}
