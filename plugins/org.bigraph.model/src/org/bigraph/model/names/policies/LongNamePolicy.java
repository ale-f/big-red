package org.bigraph.model.names.policies;

public class LongNamePolicy implements INamePolicy {
	@Override
	public String normalise(String name) {
		try {
			return Long.toString(Long.parseLong(name));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public String get(int value) {
		return Long.toString(Math.abs(value));
	}
}
