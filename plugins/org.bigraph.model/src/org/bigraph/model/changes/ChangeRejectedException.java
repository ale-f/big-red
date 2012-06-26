package org.bigraph.model.changes;

/**
 * {@link IChangeValidator}s throw {@link ChangeRejectedException}s when a
 * {@link Change} could not be applied.
 * @author alec
 *
 */
public class ChangeRejectedException extends Exception {
	private static final long serialVersionUID = 7181613421769493596L;

	private Change rejectedChange;
	private String rationale;
	
	public ChangeRejectedException(Change rejectedChange, String rationale) {
		this.rejectedChange = rejectedChange;
		this.rationale = rationale;
	}
	
	/**
	 * Gets the rejected {@link Change}.
	 * @return a {@link Change}
	 */
	public Change getRejectedChange() {
		return rejectedChange;
	}
	
	/**
	 * Gets the reason why the {@link IChangeValidator} rejected the {@link
	 * Change}.
	 * @return the rationale, which would <i>ideally</i> be human-readable
	 */
	public String getRationale() {
		return rationale;
	}
	
	@Override
	public String getMessage() {
		return "The change " + rejectedChange +
				" was rejected: " + rationale;
	}
}
