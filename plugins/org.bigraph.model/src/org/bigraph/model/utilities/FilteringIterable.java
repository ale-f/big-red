package org.bigraph.model.utilities;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FilteringIterable<T> implements Iterable<T> {
	private final Class<T> klass;
	private final Iterable<?> iterable;
	
	private final class FilteringIterator implements Iterator<T> {
		private final Iterator<?> actual = iterable.iterator();
		private T buffer;
		
		private void prepBuffer() {
			Object o;
			while (buffer == null && actual.hasNext()) {
				o = actual.next();
				if (klass.isInstance(o)) {
					buffer = klass.cast(o);
					break;
				}
			}
		}
		
		@Override
		public boolean hasNext() {
			prepBuffer();
			return (buffer != null);
		}

		@Override
		public T next() {
			prepBuffer();
			if (buffer != null) {
				T oldBuffer = buffer;
				buffer = null;
				return oldBuffer;
			} else throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public FilteringIterable(Class<T> klass, Iterable<?> iterable) {
		this.klass = klass;
		this.iterable = iterable;
	}
	
	@Override
	public Iterator<T> iterator() {
		return new FilteringIterator();
	}
}
