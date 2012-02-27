package dk.itu.big_red.model.namespaces;

public class StringNamePolicy implements INamePolicy {
	@Override
	public boolean validate(String name) {
		return true;
	}

	private String alphabet = "abcdefghijklmnopqrstuvwxyz";
	
	@Override
	public String getName(int value) {
		value = value % /* 26^6 = */ 308915776;
		String s = "";
		boolean nonZeroEncountered = false;
		for (int i = 5; i >= 0; i--) {
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
}
