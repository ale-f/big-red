package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.util.UI;

/**
 * {@link ChangeCommand}s apply a {@link Change} to an {@link IChangeable}.
 * @author alec
 *
 */
public abstract class ChangeCommand extends Command {
	private Change change;
	private IChangeable target;
	
	/**
	 * Gets the {@link Change} that will be applied by this command.
	 * @return a {@link Change}
	 */
	public Change getChange() {
		return change;
	}

	/**
	 * Sets the {@link Change} that will be applied by this command.
	 * @param change a {@link Change}
	 */
	public void setChange(Change change) {
		this.change = change;
	}

	/**
	 * Gets the {@link IChangeable} that will be modified by this command.
	 * @return an {@link IChangeable}
	 */
	public IChangeable getTarget() {
		return target;
	}

	/**
	 * Sets the {@link IChangeable} that will be modified by this command.
	 * @param target an {@link IChangeable}
	 */
	public void setTarget(IChangeable target) {
		this.target = target;
	}
	
	/**
	 * Prepares this command for execution once all of its parameters have been
	 * set.
	 */
	public abstract void prepare();
	
	/**
	 * If this command's {@link Change change} and {@link IChangeable target}
	 * have been set, and the {@link Change} is suitably configured and ready
	 * to go, returns <code>true</code>.
	 * @return <code>true</code> if this {@link ChangeCommand} is ready to be
	 * executed, or <code>false</code> otherwise
	 */
	@Override
	public final boolean canExecute() {
		boolean status = (change != null && target != null &&
				change.isReady());
		if (status) {
			status = status && target.validateChange(change);
			UI.getActiveStatusLine().setErrorMessage(status ? null :
				target.getLastRejection().getRationale());
		}
		return status;
	}
	
	/**
	 * Applies this command's {@link Change} to its {@link IChangeable target}.
	 */
	@Override
	public final void execute() {
		target.applyChange(change);
	}
	
	private Change inverse = null;
	
	/**
	 * Reverses the effects of this command's {@link Change} (by applying its
	 * {@link Change#inverse() inverse}).
	 */
	@Override
	public final void undo() {
		if (inverse == null)
			inverse = change.inverse();
		target.applyChange(inverse);
	}
	
	/**
	 * Re-applies this command's {@link Change} to its {@link IChangeable
	 * target}.
	 */
	@Override
	public final void redo() {
		execute();
	}
}
