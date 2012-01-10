package dk.itu.big_red.utilities.io;

import java.io.IOException;
import java.io.InputStream;

public class AsynchronousInputThread extends AsynchronousIOThread {
	private final IAsynchronousInputRecipient air;
	
	public AsynchronousInputThread(IAsynchronousInputRecipient air) {
		this.air = air;
	}
	
	private InputStream is;
	
	public AsynchronousInputThread setInputStream(InputStream is) {
		this.is = is;
		return this;
	}
	
	@Override
	public void run() {
		try {
			byte[] buffer = new byte[128];
			int length;
			while ((length = is.read(buffer)) != -1) {
				final byte[] tBuffer = buffer;
				final int tLength = length;
				if (!conditionalDispatch(new Runnable() {
					@Override
					public void run() {
						air.signalInput(tLength, tBuffer);
					}
				})) break;
				buffer = new byte[128];
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
