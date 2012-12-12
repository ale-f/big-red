package org.bigraph.model.utilities;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.bigraph.model.ModelObject.require;

public class FilteringIterable<T> implements Iterable<T> {
	public interface Filter<T, V> {
		T filter(V in);
	}
	
	private static final class ClassFilter<T> implements Filter<T, Object> {
		private final Class<T> klass;
		
		public ClassFilter(Class<T> klass) {
			this.klass = klass;
		}
		
		@Override
		public T filter(Object in) {
			return require(in, klass);
		}
	}
	
	private final Filter<T, Object> filter;
	private final Iterable<?> iterable;
	
	private final class FilteringIterator implements Iterator<T> {
		private final Iterator<?> actual = iterable.iterator();
		private T buffer;
		
		private void prepBuffer() {
			while (buffer == null && actual.hasNext())
				buffer = filter.filter(actual.next());
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
		this(new ClassFilter<T>(klass), iterable);
	}
	
	public FilteringIterable(Filter<T, Object> filter, Iterable<?> iterable) {
		this.filter = filter;
		this.iterable = iterable;
	}
	
	public <V> Iterable<V> filter(Class<V> klass) {
		return new FilteringIterable<V>(klass, this);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new FilteringIterator();
	}
}
