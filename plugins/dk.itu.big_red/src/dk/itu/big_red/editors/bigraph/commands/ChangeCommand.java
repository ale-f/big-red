package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChangeExecutor;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.utilities.ui.UI;

/**
 * {@link ChangeCommand}s apply a {@link Change} to an {@link IChangeExecutor}.
 * @author alec
 *
 */
public abstract class ChangeCommand extends Command {
	private Change change;
	private IChangeExecutor target;
	
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
	 * @return <code>this</code>, for convenience
	 */
	public ChangeCommand setChange(Change change) {
		this.change = change;
		return this;
	}

	/**
	 * Gets the {@link IChangeExecutor} that will be modified by this command.
	 * @return an {@link IChangeExecutor}
	 */
	public IChangeExecutor getTarget() {
		return target;
	}

	/**
	 * Sets the {@link IChangeExecutor} that will be modified by this command.
	 * @param target an {@link IChangeExecutor}
	 * @return <code>this</code>, for convenience
	 */
	public ChangeCommand setTarget(IChangeExecutor target) {
		this.target = target;
		return this;
	}
	
	/**
	 * Prepares this command for execution once all of its parameters have been
	 * set.
	 * @return <code>this</code>, for convenience
	 */
	public abstract ChangeCommand prepare();
	
	/**
	 * If this command's {@link Change change} and {@link IChangeExecutor target}
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
			try {
				target.tryValidateChange(change);
				status = true;
			} catch (ChangeRejectedException cre) {
				status = false;
				UI.getActiveStatusLine().
					setErrorMessage(status ? null : cre.getRationale());
			}
		}
		return status;
	}
	
	/**
	 * Applies this command's {@link Change} to its {@link IChangeExecutor target}.
	 */
	@Override
	public final void execute() {
		try {
			target.tryApplyChange(change);
		} catch (ChangeRejectedException cre) {
			/* do nothing */
		}
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
		try {
			target.tryApplyChange(inverse);
		} catch (ChangeRejectedException cre) {
			/* do nothing */
		}
	}
	
	/**
	 * Re-applies this command's {@link Change} to its {@link IChangeExecutor
	 * target}.
	 */
	@Override
	public final void redo() {
		execute();
	}
	
	@Override
	public void dispose() {
		if (change instanceof ChangeGroup)
			((ChangeGroup)change).clear();
		if (inverse instanceof ChangeGroup)
			((ChangeGroup)inverse).clear();
		change = inverse = null;
	}
}
