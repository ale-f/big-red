package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * ClassComparators are comparison functions that sort objects based on their
 * {@link Class}es. {@link #setClassOrder(Object...)} can be used to set the
 * sort order; two objects with the same Class compare equal.
 * <p>Because the sort order is entirely user-definable, this comparator
 * imposes orderings that are <i>wildly</i> inconsistent with {@link
 * Object#equals(Object) equals}.
 * @author alec
 *
 */
public class ClassComparator<T> implements Comparator<T> {
	private ArrayList<Class<?>> classOrder = new ArrayList<Class<?>>();
	
	/**
	 * Default constructor; does nothing.
	 */
	public ClassComparator() {
	}
	
	/**
	 * Calls {@link #setClassOrder(Class...) setClassOrder(classes)}.
	 * @param classes the class sort order
	 */
	public ClassComparator(Class<?>... classes) {
		setClassOrder(classes);
	}
	
	/**
	 * Sets the class sort order.
	 * @param classes the class sort order
	 */
	public void setClassOrder(Class<?>... classes) {
		classOrder.clear();
		for (Object i : classes)
			classOrder.add((Class<?>)i);
	}
	
	/**
	 * Sets the behaviour for objects whose classes weren't specified in the
	 * sort order.
	 * @param behaviour <code>true</code> if they should be sorted to the end,
	 * or <code>false</code> if they should be sorted to the start
	 */
	public void setUndefinedAtEnd(boolean behaviour) {
		modifier = (behaviour ? 1 : -1);
	}
	
	private int modifier = 1;
	
	@Override
	public int compare(T o1, T o2) {
		int i1 = classOrder.indexOf(o1.getClass()),
		    i2 = classOrder.indexOf(o2.getClass());
		if (i1 == -1 && i2 == -1) /* neither class has a defined position */
			return 0;
		else if (i1 == -1) /* i1's class doesn't have a defined position */
			return 1 * modifier;
		else if (i2 == -1) /* i2's class doesn't have a defined position */
			return -1 * modifier;
		else return i1 - i2;
	}

}
