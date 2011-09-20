package dk.itu.big_red.import_export;

import dk.itu.big_red.application.RedException;

/**
 * Classes extending {@link Export} throw an ExportFailedException if the
 * export went wrong for some reason.
 * @author alec
 *
 */
public class ExportFailedException extends RedException {
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
