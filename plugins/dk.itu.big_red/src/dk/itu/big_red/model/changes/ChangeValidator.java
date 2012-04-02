package dk.itu.big_red.model.changes;

/**
 * <strong>ChangeValidator</strong> is a sensible concrete implementation of
 * {@link IChangeValidator}.
 * <p>
 * @author alec
 *
 */
public abstract class ChangeValidator<T extends IChangeExecutor> implements IChangeValidator {
	private T changeable;
	
	public ChangeValidator(T changeable) {
		this.changeable = changeable;
	}
	
	/**
	 * Sets the {@link IChangeExecutor} that this {@link ChangeValidator} is acting
	 * for.
	 * @param changeable an {@link IChangeExecutor}
	 */
	public void setChangeable(T changeable) {
		this.changeable = changeable;
	}
	
	/**
	 * Gets the {@link IChangeExecutor} that this {@link ChangeValidator} is acting
	 * for.
	 * @return an {@link IChangeExecutor}
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
