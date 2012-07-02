package org.bigraph.model.changes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A ChangeGroup is a composite of many {@link Change}s.
 * @author alec
 *
 */
public class ChangeGroup extends Change implements Iterable<Change> {
	private List<Change> changes = new ArrayList<Change>();
	
	/**
	 * Adds a {@link Change} to the end of this ChangeGroup.
	 * @param c a {@link Change} to add
	 */
	public void add(Change c) {
		changes.add(c);
	}
	
	/**
	 * Adds some {@link Change}s to the end of this ChangeGroup.
	 * @param changes some {@link Change}s to add
	 */
	public void add(Change... changes) {
		for (Change c : changes)
			this.changes.add(c);
	}
	
	@Override
	public ChangeGroup clone() {
		ChangeGroup cg = new ChangeGroup();
		cg.changes = new ArrayList<Change>(changes);
		return cg;
	}
	
	public Change head() {
		return changes.get(0);
	}
	
	public ChangeGroup tail() {
		ChangeGroup cg = new ChangeGroup();
		cg.changes = changes.subList(1, size());
		return cg;
	}
	
	/**
	 * Adds a {@link Change} to the beginning of this ChangeGroup.
	 * @param c a {@link Change}s to add
	 */
	public void prepend(Change c) {
		changes.add(0, c);
	}
	
	/**
	 * Removes any instances of a {@link Change} from this {@link ChangeGroup}.
	 * @param c a {@link Change} to remove
	 */
	public void remove(Change c) {
		changes.remove(c);
	}
	
	/**
	 * Removes all {@link Change}s from this ChangeGroup.
	 */
	public void clear() {
		changes.clear();
	}
	
	@Override
	public ChangeGroup inverse() {
		ChangeGroup changes = new ChangeGroup();
		for (Change c : this)
			changes.prepend(c.inverse());
		return changes;
	}

	@Override
	public Iterator<Change> iterator() {
		return changes.iterator();
	}
	
	@Override
	public String toString() {
		return changes.toString();
	}
	
	public Change get(int position) {
		return changes.get(position);
	}
	
	public Change set(int position, Change c) {
		return changes.set(position, c);
	}
	
	/**
	 * Returns the number of {@link Change}s in this {@link ChangeGroup}.
	 * @return exactly what it says on the tin
	 */
	public int size() {
		return changes.size();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == ChangeGroup.class) {
			return changes.equals(((ChangeGroup)obj).changes);
		} else return false;
	}
	
	@Override
	public int hashCode() {
		return changes.hashCode();
	}
}
