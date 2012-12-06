package org.bigraph.model.utilities;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ArrayIterable<T> implements Iterable<T> {
	private ArrayWrapper<T> array;
	
	public ArrayIterable(T[] array) {
		this(new ArrayImpl<T>(array));
	}
	
	public ArrayIterable(ArrayWrapper<T> array) {
		this.array = array;
	}
	
	public interface ArrayWrapper<T> {
		int size();
		T get(int index) throws ArrayIndexOutOfBoundsException;
	}
	
	private static final class ArrayImpl<T> implements ArrayWrapper<T> {
		private T[] array;
		
		private ArrayImpl(T[] array) {
			this.array = array;
		}
		
		@Override
		public T get(int index) throws ArrayIndexOutOfBoundsException {
			return array[index];
		}
		
		@Override
		public int size() {
			return array.length;
		}
	}
	
	private final class ArrayIterator implements Iterator<T> {
		int i = 0;

		@Override
		public boolean hasNext() {
			return (i < array.size());
		}

		@Override
		public T next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return array.get(i++);
		}

		@Override
		public void remove() throws UnsupportedOperationException {
			throw new UnsupportedOperationException();
		}
	}
	
	private static final class NodeListImpl implements ArrayWrapper<Node> {
		private NodeList nl;
		
		private NodeListImpl(NodeList nl) {
			this.nl = nl;
		}

		@Override
		public int size() {
			return nl.getLength();
		}

		@Override
		public Node get(int index) throws ArrayIndexOutOfBoundsException {
			Node n = nl.item(index);
			if (n != null) {
				return n;
			} else throw new ArrayIndexOutOfBoundsException("" + index);
		}
	}
	
	public static ArrayIterable<? extends Node> forNodeList(NodeList nl) {
		return new ArrayIterable<Node>(new NodeListImpl(nl));
	}
	
	public <V> Iterable<V> filter(Class<V> klass) {
		return new FilteringIterable<V>(klass, this);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new ArrayIterator();
	}
}
