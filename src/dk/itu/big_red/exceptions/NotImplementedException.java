package dk.itu.big_red.exceptions;

public class NotImplementedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6210666420389245280L;

	public NotImplementedException() {
		super();
	}

	public NotImplementedException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NotImplementedException(String message) {
		super(message);
	}

	public NotImplementedException(Throwable cause) {
		super(cause);
	}

}
