package dk.itu.big_red.utilities.io.strategies;

import java.io.IOException;
import java.io.InputStream;

public class TotalReadStrategy extends DelimitedReadStrategy {
	@Override
	protected boolean shouldBreak(byte b) {
		return false;
	}

	/**
	 * Returns the contents of an {@link InputStream}, which should contain
	 * characters encoded in the system's default character set, as a {@link
	 * String}.
	 * @param is an {@link InputStream}
	 * @return a {@link String}, or <code>null</code> if something went wrong
	 */
	public static String readString(InputStream is) {
		try {
			return new String(new TotalReadStrategy().read(is));
		} catch (IOException e) {
			return null;
		}
	}
}