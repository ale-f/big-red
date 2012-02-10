package dk.itu.big_red.utilities.names;

public class PositiveIntegerNamePolicy implements INamePolicy {
	@Override
	public boolean validate(String name) {
		if (name == null)
			System.out.println("Whahuh?");
		boolean r;
		try {
			int i = Integer.parseInt(name);
			r = (i >= 1);
		} catch (NumberFormatException e) {
			r = false;
		}
		return r;
	}

	@Override
	public String getName(int value) {
		return Integer.toString(Math.abs(value) + 1);
	}
}
