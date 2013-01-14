package org.bigraph.model.utilities.comparators;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <strong>This comparator imposes orderings that are incompatible with {@link
 * Object#equals(Object) equals}.</strong>
 * @author alec
 */
public class LexicographicStringComparator
		implements Comparator<String>, Serializable {
	private static final long serialVersionUID = -5462849562338259681L;
	
	public static final LexicographicStringComparator INSTANCE =
			new LexicographicStringComparator();
	
	@Override
	public int compare(String o1, String o2) {
		return o1.compareTo(o2);
	}
}
