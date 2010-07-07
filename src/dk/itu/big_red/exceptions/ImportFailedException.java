package dk.itu.big_red.exceptions;

import dk.itu.big_red.model.import_export.Import;

/**
 * Classes extending {@link Import} throw an ImportFailedException if the
 * import went wrong for some reason.
 * @author alec
 *
 */
public class ImportFailedException extends Exception {

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
