package dk.itu.big_red.model.changes;

/**
 * {@link IChangeValidator}s throw {@link ChangeRejectedException}s when a
 * {@link Change} could not be applied.
 * @author alec
 *
 */
public class ChangeRejectedException extends Exception {
	private static final long serialVersionUID = 7181613421769493596L;

	private IChangeable changeable;
	private Change rejectedChange;
	private IChangeValidator rejector;
	private String rationale;
	
	public ChangeRejectedException(IChangeable changeable, Change rejectedChange, IChangeValidator rejector, String rationale) {
		this.changeable = changeable;
		this.rejectedChange = rejectedChange;
		this.rejector = rejector;
		this.rationale = rationale;
	}
	
	/**
	 * Gets the {@link IChangeable} that was the target of the rejected {@link
	 * Change}.
	 * @return an {@link IChangeable}
	 */
	public IChangeable getChangeable() {
		return changeable;
	}
	
	/**
	 * Gets the rejected {@link Change}.
	 * @return a {@link Change}
	 */
	public Change getRejectedChange() {
		return rejectedChange;
	}
	
	/**
	 * Gets the {@link IChangeValidator} that rejected the {@link Change}.
	 * @return an {@link IChangeValidator}
	 */
	public IChangeValidator getRejector() {
		return rejector;
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
		return "The attempt to change " + changeable +
				" by applying change " + rejectedChange +
				" was rejected by " + rejector +
				", which gave the rationale \"" + rationale + "\".";
	}
}
