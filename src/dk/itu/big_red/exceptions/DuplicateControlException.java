package dk.itu.big_red.exceptions;

import dk.itu.big_red.model.ControlAuthority;

/**
 * It's a serious (and stupid) error to instantiate two Controls with the same
 * name; if that happens, a DuplicateControlException will be raised. To get
 * another copy of a pre-existing Controls, use {@link ControlAuthority#getControl}.
 * @author alec
 *
 */
public class DuplicateControlException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6058413080370813190L;

	public DuplicateControlException() {
		super();
	}

	public DuplicateControlException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateControlException(String message) {
		super(message);
	}

	public DuplicateControlException(Throwable cause) {
		super(cause);
	}
}
