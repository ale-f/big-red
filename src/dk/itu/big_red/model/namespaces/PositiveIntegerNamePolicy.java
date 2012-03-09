package dk.itu.big_red.model.namespaces;

public class PositiveIntegerNamePolicy implements INamePolicy {
	@Override
	public boolean validate(String name) {
		return (normalise(name) != null);
	}

	@Override
	public String get(int value) {
		return Integer.toString(Math.abs(value) + 1);
	}
	
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
}
