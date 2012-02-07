package dk.itu.big_red.utilities;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class IterableWrapper<T> implements Iterable<T> {
	protected abstract T item(int index);
	
	protected abstract int count();
	
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int position = 0;
			
			@Override
			public boolean hasNext() {
				return (position < count());
			}

			@Override
			public T next() {
				if (hasNext()) {
					return item(position++);
				} else throw new NoSuchElementException();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public static <T> Iterable<T> createIterable(final Iterator<T> iterator) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}
	
	public static <T> Iterator<T> createArrayIterator(final T[] array) {
		return new IterableWrapper<T>() {
			@Override
			protected int count() {
				return array.length;
			}
			@Override
			protected T item(int index) {
				return array[index];
			}
		}.iterator();
	}
}
