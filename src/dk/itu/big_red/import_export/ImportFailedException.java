package dk.itu.big_red.import_export;

import dk.itu.big_red.utilities.RedException;

/**
 * Classes extending {@link Import} throw an ImportFailedException if the
 * import went wrong for some reason.
 * @author alec
 *
 */
public class ImportFailedException extends RedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9143775624800236826L;

	public ImportFailedException() {
	}

	public ImportFailedException(String message) {
		super(message);
	}

	public ImportFailedException(Throwable cause) {
		super(cause);
	}

	public ImportFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
