package org.bigraph.model.changes;

import org.bigraph.model.assistants.PropertyScratchpad;

/**
 * A Change is a reversible modification.
 * @author alec
 */
public abstract class Change implements IChange {
	@Override
	public abstract Change inverse();
	
	@Override
	public void beforeApply() {
	}
	
	@Override
	public void simulate(PropertyScratchpad context) {
		throw new UnsupportedOperationException("" + this +
				" doesn't support the simulate(PropertyScratchpad) method");
	}
	
	/**
	 * A {@link Change} which can't be inverted and isn't ready to be executed.
	 */
	public static final Change INVALID = new Change() {
		@Override
		public Change inverse() {
			return this;
		}
		
		@Override
		public String toString() {
			return "Change(invalid)";
		}
	};
}
