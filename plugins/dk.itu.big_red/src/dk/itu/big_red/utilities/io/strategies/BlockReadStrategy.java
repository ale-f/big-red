package dk.itu.big_red.utilities.io.strategies;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BlockReadStrategy implements IReadStrategy {
	private int maxSize;
	
	public BlockReadStrategy() {
		maxSize = 128;
	}
	
	public BlockReadStrategy(int maxSize) {
		this.maxSize = Math.max(1, maxSize);
	}
	
	@Override
	public byte[] read(InputStream is) throws IOException {
		byte[] buffer = new byte[maxSize];
		int r = is.read(buffer);
		return (r != -1 ?
				(r == maxSize ? buffer : Arrays.copyOf(buffer, r)) : null);
	}
}
