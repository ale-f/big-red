package dk.itu.big_red.util;

/**
 * All exceptions thrown by Big Red are subclasses of <code>RedException</code>.
 * @author alec
 *
 */
public class RedException extends Exception {
	private static final long serialVersionUID = 1L;

	public RedException() {
		super();
	}

	public RedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RedException(String message) {
		super(message);
	}

	public RedException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * Prints this exception as though by {@link #printStackTrace()}, and
	 * promptly kills the Java VM in a huff.
	 */
	public void killVM() {
		printStackTrace();
		System.exit(-1);
	}
}
