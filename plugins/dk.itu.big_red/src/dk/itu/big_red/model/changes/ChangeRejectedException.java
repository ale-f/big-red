package dk.itu.big_red.model.changes;

/**
 * {@link IChangeValidator}s throw {@link ChangeRejectedException}s when a
 * {@link Change} could not be applied.
 * @author alec
 *
 */
public class ChangeRejectedException extends Exception {
	private static final long serialVersionUID = 7181613421769493596L;

	private IChangeExecutor changeExecutor;
	private Change rejectedChange;
	private IChangeValidator rejector;
	private String rationale;
	
	public ChangeRejectedException(IChangeExecutor changeExecutor, Change rejectedChange, IChangeValidator rejector, String rationale) {
		this.changeExecutor = changeExecutor;
		this.rejectedChange = rejectedChange;
		this.rejector = rejector;
		this.rationale = rationale;
	}
	
	/**
	 * Gets the {@link IChangeExecutor} that was the target of the rejected {@link
	 * Change}.
	 * @return an {@link IChangeExecutor}
	 */
	public IChangeExecutor getChangeable() {
		return changeExecutor;
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
		return "The attempt to change " + changeExecutor +
				" by applying change " + rejectedChange +
				" was rejected by " + rejector +
				", which gave the rationale \"" + rationale + "\".";
	}
}
