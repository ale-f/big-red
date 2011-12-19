package dk.itu.big_red.util;

import java.io.IOException;
import java.io.InputStream;

public class AsynchronousInputThread extends Thread {
	private final IAsynchronousInputRecipient air;
	
	public AsynchronousInputThread(IAsynchronousInputRecipient air) {
		this.air = air;
	}
	
	private InputStream is;
	
	public AsynchronousInputThread setInputStream(InputStream is) {
		this.is = is;
		return this;
	}
	
	private Boolean go = true;
	
	public void kill() {
		synchronized (go) {
			go = false;
		}
	}
	
	private boolean conditionalDispatch(Runnable r) {
		synchronized (go) {
			if (go)
				UI.asyncExec(r);
			return go;
		}
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
						air.signalData(tLength, tBuffer);
					}
				})) break;
				buffer = new byte[128];
			}
			conditionalDispatch(new Runnable() {
				@Override
				public void run() {
					air.signalDataComplete();
				}
			});
		} catch (final IOException e) {
			conditionalDispatch(new Runnable() {
				@Override
				public void run() {
					air.signalError(e);
				}
			});
		} finally {
			try {
				is.close();
			} catch (final IOException e) {
				conditionalDispatch(new Runnable() {
					@Override
					public void run() {
						air.signalError(e);
					}
				});
			}
		}
	}
}
