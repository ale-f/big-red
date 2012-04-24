package dk.itu.big_red.utilities.io;

import java.io.IOException;

public class AsynchronousAdapter
	implements IAsynchronousInputRecipient, IAsynchronousOutputRecipient {
	@Override
	public void signalOutputError(IOException e) {
	}

	@Override
	public void signalInput(int length, byte[] buffer) {
	}

	@Override
	public void signalInputComplete() {
	}

	@Override
	public void signalInputError(IOException e) {
	}
}
