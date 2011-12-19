package dk.itu.big_red.util;

import java.io.IOException;

public interface IAsynchronousInputRecipient {
	public void signalData(int length, byte[] buffer);

	public void signalDataComplete();

	public void signalError(IOException e);
}
