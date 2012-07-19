package org.bigraph.model.names.policies;

public class PositiveIntegerNamePolicy extends BoundedIntegerNamePolicy {
	public PositiveIntegerNamePolicy() {
		super(1, Integer.MAX_VALUE);
	}
}
