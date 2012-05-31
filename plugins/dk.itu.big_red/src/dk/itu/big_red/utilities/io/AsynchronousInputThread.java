package dk.itu.big_red.utilities.io;

import java.io.IOException;
import java.io.InputStream;

import dk.itu.big_red.utilities.io.strategies.IReadStrategy;

public class AsynchronousInputThread extends AbstractAsynchronousIOThread {
	private final IAsynchronousInputRecipient air;
	
	public AsynchronousInputThread(IAsynchronousInputRecipient air) {
		this.air = air;
	}
	
	private InputStream is;
	
	public AsynchronousInputThread setInputStream(InputStream is) {
		this.is = is;
		return this;
	}
	
	private IReadStrategy readStrategy;
	
	public AsynchronousInputThread setReadStrategy(IReadStrategy readStrategy) {
		this.readStrategy = readStrategy;
		return this;
	}
	
	@Override
	public void run() {
		try {
			byte[] buffer;
			while ((buffer = readStrategy.read(is)) != null) {
				final byte[] tBuffer = buffer;
				if (!conditionalDispatch(new Runnable() {
					@Override
					public void run() {
						air.signalInput(tBuffer.length, tBuffer);
					}
				})) break;
			}
			conditionalDispatch(new Runnable() {
				@Override
				public void run() {
					air.signalInputComplete();
				}
			});
		} catch (final IOException e) {
			conditionalDispatch(new Runnable() {
				@Override
				public void run() {
					air.signalInputError(e);
				}
			});
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
				conditionalDispatch(new Runnable() {
					@Override
					public void run() {
						air.signalInputError(e);
					}
				});
			}
		}
	}
}
