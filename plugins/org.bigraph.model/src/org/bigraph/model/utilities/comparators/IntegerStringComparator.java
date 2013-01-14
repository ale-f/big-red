package org.bigraph.model.utilities.comparators;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <strong>This comparator imposes orderings that are incompatible with {@link
 * Object#equals(Object) equals}.</strong>
 * @author alec
 */
public class IntegerStringComparator
		implements Comparator<String>, Serializable {
	private static final long serialVersionUID = 197652743484967772L;
	
	public static final IntegerStringComparator INSTANCE =
			new IntegerStringComparator();
	
	@Override
	public final int compare(String a_, String b_) {
		return Integer.parseInt(a_) - Integer.parseInt(b_);
	}
}
