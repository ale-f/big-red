package dk.itu.big_red.utilities.io;

import java.io.IOException;

public interface IAsynchronousInputRecipient {
	/**
	 * Called (in the main thread) when the worker thread has finished a read
	 * operation.
	 * @param length the number of bytes actually present in <code>buffer</code>
	 * @param buffer a buffer containing a number of bytes
	 */
	void signalInput(int length, byte[] buffer);

	/**
	 * Called (in the main thread) when there's nothing left for the worker
	 * thread to read.
	 */
	void signalInputComplete();

	/**
	 * Called (in the main thread) when the worker thread encounters an error.
	 * @param e an {@link IOException}
	 */
	void signalInputError(IOException e);
}
