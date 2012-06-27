package dk.itu.big_red.utilities.io;

import java.io.IOException;

public interface IAsynchronousOutputRecipient {
	/**
	 * Called (in the main thread) when the worker thread encounters an error.
	 * @param e an {@link IOException}
	 */
	void signalOutputError(IOException e);
}
