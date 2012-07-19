package org.bigraph.model.names.policies;

public class BoundedIntegerNamePolicy implements INamePolicy {
	private final int min, max;
	private final long range;
	
	public BoundedIntegerNamePolicy() {
		this(1, Integer.MAX_VALUE);
	}
	
	public BoundedIntegerNamePolicy(int min) {
		this(min, Integer.MAX_VALUE);
	}
	
	public BoundedIntegerNamePolicy(int min, int max) {
		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
		range = (this.max - this.min) + 1;
	}
	
	@Override
	public String normalise(String name) {
		try {
			int i = Integer.parseInt(name);
			if (i >= min && i <= max)
				return Integer.toString(i);
		} catch (NumberFormatException e) {
			/* fall through */
		}
		return null;
	}
	
	@Override
	public String get(int value) {
		return Long.toString(min + (Math.abs(value) % range));
	}
}
