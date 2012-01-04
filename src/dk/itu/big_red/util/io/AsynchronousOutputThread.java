package dk.itu.big_red.util.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class AsynchronousOutputThread extends AsynchronousIOThread {
	private IAsynchronousOutputRecipient aor;
	
	public AsynchronousOutputThread(IAsynchronousOutputRecipient aor) {
		this.aor = aor;
	}
	
	private boolean bufferFinished = false;
	private ArrayList<byte[]> buffer = new ArrayList<byte[]>();
	
	private OutputStream os;
	
	public AsynchronousOutputThread setOutputStream(OutputStream os) {
		this.os = os;
		return this;
	}
	
	@Override
	public void kill() {
		super.kill();
		synchronized (buffer) {
			buffer.notify();
		}
	}
	
	public void enqueue(byte[] buf) {
		synchronized (buffer) {
			if (buf == null) {
				bufferFinished = true;
				buffer.add(buf);
			} else if (!bufferFinished) {
				buffer.add(buf);
			}
			buffer.notify();
		}
	}
	
	@Override
	public void run() {
		try {
			do {
				synchronized (running) {
					if (!running)
						return;
				}
				
				byte[] buf;
				synchronized (buffer) {
					while (buffer.size() == 0) {
						try {
							buffer.wait();
							synchronized (running) {
								if (!running)
									return;
							}
						} catch (InterruptedException e) {
							/* do nothing */
						}
					}
					buf = buffer.remove(0);
				}
				
				if (buf == null)
					break;
				
				try {
					os.write(buf);
					os.flush();
				} catch (final IOException ex) {
					conditionalDispatch(new Runnable() {
						@Override
						public void run() {
							aor.signalOutputError(ex);
						}
					});
					kill();
				}
			} while (true);
		} finally {
			try {
				os.flush();
				os.close();
			} catch (final IOException ex) {
				conditionalDispatch(new Runnable() {
					@Override
					public void run() {
						aor.signalOutputError(ex);
					}
				});
			}
		}
	}
}
