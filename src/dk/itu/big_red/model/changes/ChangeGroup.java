package dk.itu.big_red.model.changes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A ChangeGroup is a composite of many {@link Change}s.
 * @author alec
 *
 */
public class ChangeGroup extends Change implements Iterable<Change> {
	private ArrayList<Change> changes =
		new ArrayList<Change>();
	
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
	
	/**
	 * Adds some {@link Change}s to the end of this ChangeGroup.
	 * @param changes some {@link Change}s to add
	 */
	public void add(Collection<? extends Change> changes) {
		this.changes.addAll(changes);
	}
	
	/**
	 * Adds a {@link Change} to the beginning of this ChangeGroup.
	 * @param c a {@link Change}s to add
	 */
	public void prepend(Change c) {
		changes.add(0, c);
	}
	
	@Override
	public boolean isReady() {
		if (changes.isEmpty())
			return false;
		for (Change c : this)
			if (!c.isReady())
				return false;
		return true;
	}
	
	/**
	 * Removes all {@link Change}s from this ChangeGroup.
	 */
	public void clear() {
		changes.clear();
	}
	
	@Override
	public Change inverse() {
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
}
