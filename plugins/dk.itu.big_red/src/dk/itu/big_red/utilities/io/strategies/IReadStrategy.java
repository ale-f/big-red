package dk.itu.big_red.utilities.io.strategies;

import java.io.IOException;
import java.io.InputStream;

/**
 * Classes implementing <strong>IReadStrategy</strong> have a strategy for
 * reading data from an {@link InputStream}.
 * @author alec
 */
public interface IReadStrategy {
	/**
	 * Reads some data from the given {@link InputStream}.
	 * @param is an {@link InputStream}
	 * @return an array of bytes, exactly as long as the number of bytes read
	 * from the stream
	 * @throws IOException if an I/O error occurs
	 */
	byte[] read(InputStream is) throws IOException;
}
