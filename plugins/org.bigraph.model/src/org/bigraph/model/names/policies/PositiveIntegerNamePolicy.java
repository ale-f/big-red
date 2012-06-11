package org.bigraph.model.names.policies;

public class PositiveIntegerNamePolicy implements INamePolicy {
	@Override
	public String normalise(String name) {
		try {
			int i = Integer.parseInt(name);
			if (i >= 1)
				return Integer.toString(i);
		} catch (NumberFormatException e) {
			/* fall through */
		}
		return null;
	}
	
	@Override
	public String get(int value) {
		return Integer.toString(Math.abs(value) + 1);
	}
}
