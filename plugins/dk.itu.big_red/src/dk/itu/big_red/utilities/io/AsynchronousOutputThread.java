package dk.itu.big_red.utilities.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class AsynchronousOutputThread extends AbstractAsynchronousIOThread {
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
	
	public void add(byte[] buf) {
		synchronized (buffer) {
			if (!bufferFinished && buf != null)
				buffer.add(buf);
			buffer.notify();
		}
	}
	
	public void add(String buf) {
		if (!bufferFinished && buf != null) {
			ByteBuffer bb = Charset.defaultCharset().encode(buf);
			byte[] a = new byte[bb.remaining()];
			bb.get(a);
			add(a);
		}
	}
	
	public void done() {
		synchronized (buffer) {
			if (!bufferFinished) {
				bufferFinished = true;
				buffer.add(null);
				buffer.notify();
			}
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
