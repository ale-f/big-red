package org.bigraph.model.changes;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;

/**
 * A Change is a reversible modification.
 * @author alec
 */
@Deprecated
public abstract class Change implements IChange {
	@Override
	public void simulate(PropertyScratchpad context, Resolver resolver) {
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
