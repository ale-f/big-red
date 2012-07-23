package org.bigraph.model.savers;

/**
 * Classes extending {@link Saver} throw an SaveFailedException if the
 * export went wrong for some reason.
 * @author alec
 */
public class SaveFailedException extends Exception {
	private static final long serialVersionUID = 8144216377045944049L;

	public SaveFailedException() {
	}

	public SaveFailedException(String message) {
		super(message);
	}

	public SaveFailedException(Throwable cause) {
		super(cause);
	}

	public SaveFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
