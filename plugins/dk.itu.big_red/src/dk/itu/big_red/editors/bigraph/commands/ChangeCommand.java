package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.utilities.ui.UI;

/**
 * {@link IChangeCommand}s apply a {@link IChange} to an {@link
 * IChangeExecutor}.
 * @author alec
 */
public class ChangeCommand extends Command {
	private IChange change;
	private Object target;
	
	public ChangeCommand() {
	}
	
	public ChangeCommand(IChange change, Object target) {
		this.change = change;
		this.target = target;
	}
	
	/**
	 * Gets the {@link IChange} that will be applied by this command.
	 * @return a {@link IChange}
	 */
	public IChange getChange() {
		return change;
	}

	/**
	 * Sets the {@link IChange} that will be applied by this command.
	 * @param change a {@link IChange}
	 * @return <code>this</code>, for convenience
	 */
	public ChangeCommand setChange(IChange change) {
		this.change = change;
		return this;
	}

	/**
	 * Gets the {@link IChangeExecutor} that will be modified by this command.
	 * @return an {@link IChangeExecutor}
	 */
	public Object getTarget() {
		return target;
	}

	/**
	 * Sets the {@link IChangeExecutor} that will be modified by this command.
	 * @param target an {@link IChangeExecutor}
	 * @return <code>this</code>, for convenience
	 */
	public ChangeCommand setTarget(Object target) {
		this.target = target;
		return this;
	}
	
	/**
	 * Prepares this command for execution once all of its parameters have been
	 * set.
	 * @return <code>this</code>, for convenience
	 */
	public void prepare() {
	}
	
	/**
	 * If this command's {@link IChange change} and {@link IChangeExecutor
	 * target} have been set, and the {@link IChange} is suitably configured
	 * and ready to go, returns <code>true</code>.
	 * @return <code>true</code> if this {@link IChangeCommand} is ready to be
	 * executed, or <code>false</code> otherwise
	 */
	@Override
	public final boolean canExecute() {
		IChange change = getChange();
		if (change instanceof ChangeGroup &&
				((ChangeGroup)change).size() == 0)
			return false;
		boolean status = false;
		try {
			ExecutorManager.getInstance().tryValidateChange(change);
			status = true;
		} catch (ChangeRejectedException cre) {
			UI.getActiveStatusLine().setErrorMessage(cre.getRationale());
		}
		return status;
	}
	
	/**
	 * Applies this command's {@link IChange} to its {@link IChangeExecutor
	 * target}.
	 */
	@Override
	public final void execute() {
		try {
			ExecutorManager.getInstance().tryApplyChange(getChange());
			UI.getActiveStatusLine().setErrorMessage(null);
		} catch (ChangeRejectedException cre) {
			/* do nothing */
		}
	}
	
	private IChange inverse = null;
	
	protected IChange getInverse() {
		if (inverse == null)
			inverse = getChange().inverse();
		return inverse;
	}
	
	/**
	 * Reverses the effects of this command's {@link IChange} (by applying its
	 * {@link IChange#inverse() inverse}).
	 */
	@Override
	public final void undo() {
		try {
			ExecutorManager.getInstance().tryApplyChange(getInverse());
		} catch (ChangeRejectedException cre) {
			/* do nothing */
		}
	}
	
	/**
	 * Re-applies this command's {@link IChange} to its {@link IChangeExecutor
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
	
	@Override
	public String toString() {
		return "ChangeCommand(" + getChange() + ")";
	}
	
	@Override
	public String getLabel() {
		return toString();
	}
}
