package dk.itu.big_red.exceptions;

import dk.itu.big_red.util.DOM;

/**
 * ValidationFailedExceptions are thrown when {@link
 * DOM#validate(org.w3c.dom.Document, java.io.InputStream)} fails to validate
 * a document.
 * @author alec
 *
 */
public class ValidationFailedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2568746474807403861L;

	public ValidationFailedException() {
	}

	public ValidationFailedException(String message) {
		super(message);
	}

	public ValidationFailedException(Throwable cause) {
		super(cause);
	}

	public ValidationFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
