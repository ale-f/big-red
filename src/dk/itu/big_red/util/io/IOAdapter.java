package dk.itu.big_red.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class IOAdapter {
	private boolean bufferClosed = false;
	private ArrayList<Integer> buffer = new ArrayList<Integer>();
	
	private class IOAdapterInput extends InputStream {

		@Override
		public int read() throws IOException {
			synchronized (buffer) {
				try {
					while (buffer.size() == 0)
						buffer.wait();
				} catch (InterruptedException e) {
					throw new IOException(e);
				}
				Integer i = buffer.get(0);
				if (i == -1) {
					if (!bufferClosed)
						throw new IOException("Invalid byte in stream");
				} else buffer.remove(0);
				return i;
			}
		}
		
	}
	
	private class IOAdapterOutput extends OutputStream {

		@Override
		public void write(int i) throws IOException {
			synchronized (buffer) {
				if (!bufferClosed) {
					buffer.add(i & 0xFF);
					buffer.notify();
				} else throw new IOException("The stream is closed");
			}
		}
		
		@Override
		public void close() throws IOException {
			synchronized (buffer) {
				if (!bufferClosed) {
					buffer.add(-1);
					bufferClosed = true;
					buffer.notify();
				} else throw new IOException("The stream is closed");
			}
		}
		
	}
	
	public InputStream getInputStream() {
		return new IOAdapterInput();
	}
	
	public OutputStream getOutputStream() {
		return new IOAdapterOutput();
	}
	
	private static final InputStream nullInputStream = new InputStream() {
		@Override
		public int read() throws IOException {
			// TODO Auto-generated method stub
			return -1;
		}
	};
	
	/**
	 * Returns an {@link InputStream} with no data; all calls to {@link
	 * InputStream#read()} return <code>-1</code>.
	 * @return an empty {@link InputStream}
	 */
	public static InputStream getNullInputStream() {
		return nullInputStream;
	}
}
