package dk.itu.big_red.exceptions;

import dk.itu.big_red.import_export.Export;

/**
 * Classes extending {@link Export} throw an ExportFailedException if the
 * export went wrong for some reason.
 * @author alec
 *
 */
public class ExportFailedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8144216377045944049L;

	public ExportFailedException() {
	}

	public ExportFailedException(String message) {
		super(message);
	}

	public ExportFailedException(Throwable cause) {
		super(cause);
	}

	public ExportFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
