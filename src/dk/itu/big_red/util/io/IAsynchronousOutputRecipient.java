package dk.itu.big_red.util.io;

import java.io.IOException;

public interface IAsynchronousOutputRecipient {
	/**
	 * Called (in the main thread) when the worker thread encounters an error.
	 * @param e an {@link IOException}
	 */
	public void signalOutputError(IOException e);
}
