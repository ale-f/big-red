package dk.itu.big_red.utilities.io.strategies;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

abstract class DelimitedReadStrategy implements IReadStrategy {
	protected abstract boolean shouldBreak(byte b);
	
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
}