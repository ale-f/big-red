package dk.itu.big_red.utilities.io;

import java.io.IOException;
import java.io.InputStream;

public class LineReadStrategy implements IReadStrategy {
	@Override
	public byte[] read(InputStream is) throws IOException {
		int length = 0, allocated = 1;
		byte[] buffer = new byte[1],
				i = new byte[1],
				tmp;
		
		while (is.read(i) != -1) {
			byte b = i[0];
			
			if (length == allocated) {
				allocated *= 2;
				tmp = new byte[allocated];
				System.arraycopy(buffer, 0, tmp, 0, length);
				buffer = tmp;
			}
			
			buffer[length++] = b;
			if (b == '\n')
				break;
		}
		
		if (length > 0) {
			tmp = new byte[length];
			System.arraycopy(buffer, 0, tmp, 0, length);
			return tmp;
		} else return null;
	}
}
