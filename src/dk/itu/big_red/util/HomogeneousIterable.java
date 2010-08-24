package dk.itu.big_red.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * HomogenousIterables iterate through a generic {@link Iterable}, returning
 * only those elements which are instances of T.
 * @author alec
 *
 * @param <T> the type to select
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class HomogeneousIterable<T> implements Iterable<T> {
	private Class<T> tClass;
	private Iterable collection;
	
	public HomogeneousIterable(Iterable collection, Class<T> tClass) {
		this.tClass = tClass;
		this.collection = collection;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Iterator underlyingIterator = collection.iterator();
			private T next_ = null;
			
			private void zoom() {
				if (next_ != null)
					return;
				while (next_ == null && underlyingIterator.hasNext()) {
					Object o = underlyingIterator.next();
					if (tClass.isInstance(o))
						next_ = (T)o;
				}
			}
			
			@Override
			public boolean hasNext() {
				zoom();
				return (next_ != null);
			}

			@Override
			public T next() {
				zoom();
				T a = next_;
				if (a == null)
					throw new NoSuchElementException();
				next_ = null;
				return a;
			}

			@Override
			public void remove() {
				underlyingIterator.remove();
			}
		};
	}

}
