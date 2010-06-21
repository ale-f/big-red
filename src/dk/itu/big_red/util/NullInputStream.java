package dk.itu.big_red.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * The NullInputStream is a particularly stupid InputStream that does nothing;
 * it simply says "stream finished" whenever you try to read from it.
 * @author alec
 *
 */
public class NullInputStream extends InputStream {
	private NullInputStream() {}

	@Override
	public int read() throws IOException {
		return -1;
	}

	private static final NullInputStream INSTANCE = new NullInputStream();
	
	public static NullInputStream getInstance() {
		return INSTANCE;
	}
}
