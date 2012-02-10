package dk.itu.big_red.utilities.names;

public class StringNamePolicy implements INamePolicy {
	@Override
	public boolean validate(String name) {
		return true;
	}

	private String alphabet = "abcdefghijklmnopqrstuvwxyz";
	
	@Override
	public String getName(int value) {
		String s = "";
		boolean nonZeroEncountered = false;
		for (int i = 6; i >= 0; i--) {
			int y = (int)Math.pow(26, i);
			int z = value / y;

			if (z == 0 && !nonZeroEncountered && i != 0)
				continue;

			nonZeroEncountered = true;
			s += alphabet.charAt(z);

			value -= y * z;
		}
		return s;
	}

	@Override
	public StringNamePolicy clone() {
		return new StringNamePolicy();
	}
}
