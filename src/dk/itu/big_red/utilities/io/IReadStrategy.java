package dk.itu.big_red.utilities.io;

import java.io.IOException;
import java.io.InputStream;

public interface IReadStrategy {
	public byte[] read(InputStream is) throws IOException;
}
