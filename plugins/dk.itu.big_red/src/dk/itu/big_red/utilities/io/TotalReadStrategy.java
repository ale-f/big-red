package dk.itu.big_red.utilities.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class TotalReadStrategy implements IReadStrategy {
	protected boolean shouldBreak(byte b) {
		return false;
	}
	
	@Override
	public byte[] read(InputStream is) throws IOException {
		int length = 0, allocated = 1;
		byte[] buffer = new byte[1],
				i = new byte[1];
		
		while (is.read(i) != -1) {
			byte b = i[0];
			
			if (length == allocated) {
				allocated *= 2;
				buffer = Arrays.copyOf(buffer, allocated);
			}
			
			buffer[length++] = b;
			if (shouldBreak(b))
				break;
		}
		
		if (length > 0) {
			return Arrays.copyOf(buffer, length);
		} else return null;
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