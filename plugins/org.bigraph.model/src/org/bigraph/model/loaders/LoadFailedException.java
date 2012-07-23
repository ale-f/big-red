package org.bigraph.model.loaders;

/**
 * Classes extending {@link Loader} throw an LoadFailedException if the
 * import went wrong for some reason.
 * @author alec
 */
public class LoadFailedException extends Exception {
	private static final long serialVersionUID = 9143775624800236826L;

	public LoadFailedException() {
	}

	public LoadFailedException(String message) {
		super(message);
	}

	public LoadFailedException(Throwable cause) {
		super(cause);
	}

	public LoadFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}
