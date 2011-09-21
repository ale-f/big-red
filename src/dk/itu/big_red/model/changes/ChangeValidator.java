package dk.itu.big_red.model.changes;

/**
 * This is a sensible concrete implementation of most of {@link
 * IChangeValidator}; subclasses only need to implement {@link
 * #tryValidateChange(Change)}.
 * <p>
 * @author alec
 *
 */
public abstract class ChangeValidator implements IChangeValidator {
	private ChangeRejectedException lastRejection = null;
	private IChangeable changeable;
	
	/**
	 * Sets the {@link IChangeable} that this {@link ChangeValidator} is acting
	 * for.
	 * @param changeable an {@link IChangeable}
	 */
	public void setChangeable(IChangeable changeable) {
		this.changeable = changeable;
	}
	
	/**
	 * Gets the {@link IChangeable} that this {@link ChangeValidator} is acting
	 * for.
	 * @return an {@link IChangeable}
	 */
	public IChangeable getChangeable() {
		return changeable;
	}
	
	@Override
	public boolean validateChange(Change b) {
		try {
			tryValidateChange(b);
		} catch (ChangeRejectedException e) {
			lastRejection = e;
			return false;
		}
		return true;
	}

	@Override
	public ChangeRejectedException getLastRejection() {
		return lastRejection;
	}

	@Override
	public abstract void tryValidateChange(Change b) throws ChangeRejectedException;
}
