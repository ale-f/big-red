package dk.itu.big_red.model.changes;

/**
 * <strong>ChangeValidator</strong> is a sensible concrete implementation of
 * {@link IChangeValidator}.
 * <p>
 * @author alec
 *
 */
public abstract class ChangeValidator<T extends IChangeable> implements IChangeValidator {
	private T changeable;
	
	public ChangeValidator(T changeable) {
		this.changeable = changeable;
	}
	
	/**
	 * Sets the {@link IChangeable} that this {@link ChangeValidator} is acting
	 * for.
	 * @param changeable an {@link IChangeable}
	 */
	public void setChangeable(T changeable) {
		this.changeable = changeable;
	}
	
	/**
	 * Gets the {@link IChangeable} that this {@link ChangeValidator} is acting
	 * for.
	 * @return an {@link IChangeable}
	 */
	public T getChangeable() {
		return changeable;
	}
	
	/**
	 * Rejects a {@link Change}.
	 * @param b the {@link Change}
	 * @param rationale why the {@link Change} was rejected
	 * @throws ChangeRejectedException always and forever
	 */
	protected void rejectChange(Change b, String rationale) throws ChangeRejectedException {
		throw new ChangeRejectedException(getChangeable(), b, this, rationale);
	}
	
	@Override
	public abstract void tryValidateChange(Change b) throws ChangeRejectedException;
}
