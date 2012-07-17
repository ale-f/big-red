package org.bigraph.model.changes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bigraph.model.assistants.PropertyScratchpad;

/**
 * A ChangeGroup is a composite of many {@link IChange}s.
 * @author alec
 *
 */
public class ChangeGroup implements IChange, Iterable<IChange> {
	private List<IChange> changes = new ArrayList<IChange>();
	
	/**
	 * Adds a {@link Change} to the end of this ChangeGroup.
	 * @param c a {@link Change} to add
	 */
	public void add(IChange c) {
		changes.add(c);
	}
	
	/**
	 * Adds some {@link Change}s to the end of this ChangeGroup.
	 * @param changes some {@link Change}s to add
	 */
	public void add(IChange... changes) {
		for (IChange c : changes)
			this.changes.add(c);
	}
	
	@Override
	public ChangeGroup clone() {
		ChangeGroup cg = new ChangeGroup();
		cg.changes = new ArrayList<IChange>(changes);
		return cg;
	}
	
	public IChange head() {
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
	public void prepend(IChange c) {
		changes.add(0, c);
	}
	
	/**
	 * Removes any instances of a {@link Change} from this {@link ChangeGroup}.
	 * @param c a {@link Change} to remove
	 */
	public void remove(IChange c) {
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
		for (IChange c : this)
			changes.prepend(c.inverse());
		return changes;
	}

	@Override
	public Iterator<IChange> iterator() {
		return changes.iterator();
	}
	
	@Override
	public String toString() {
		return changes.toString();
	}
	
	public IChange get(int position) {
		return changes.get(position);
	}
	
	public IChange set(int position, Change c) {
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
	
	@Override
	public void simulate(PropertyScratchpad context) {
		for (IChange c : changes)
			c.simulate(context);
	}

	@Override
	public boolean canInvert() {
		for (IChange c : changes)
			if (!c.canInvert())
				return false;
		return true;
	}

	@Override
	public void beforeApply() {
		/* do nothing */
	}

	@Override
	public boolean isReady() {
		for (IChange c : changes)
			if (!c.isReady())
				return false;
		return true;
	}
}
