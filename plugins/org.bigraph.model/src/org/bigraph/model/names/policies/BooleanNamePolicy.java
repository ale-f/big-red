package org.bigraph.model.names.policies;

public class BooleanNamePolicy implements INamePolicy {
	@Override
	public String normalise(String name) {
		return Boolean.toString(Boolean.parseBoolean(name));
	}

	@Override
	public String get(int value) {
		return (value % 2 == 0 ? "false" : "true");
	}
}
