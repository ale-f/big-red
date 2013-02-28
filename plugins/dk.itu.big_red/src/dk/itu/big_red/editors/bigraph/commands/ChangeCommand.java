package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.utilities.ui.UI;

/**
 * {@link ChangeCommand}s apply an {@link ChangeDescriptorGroup}.
 * @author alec
 */
public class ChangeCommand extends Command {
	private IChangeDescriptor change;
	private Resolver resolver;
	
	public ChangeCommand() {
	}
	
	public ChangeCommand(IChangeDescriptor change, Resolver resolver) {
		this.change = change;
		this.resolver = resolver;
	}
	
	/**
	 * Gets the {@link IChangeDescriptor} that will be applied by this command.
	 * @return a {@link IChangeDescriptor}
	 */
	public IChangeDescriptor getChange() {
		return change;
	}

	/**
	 * Sets the {@link IChange} that will be applied by this command.
	 * @param change a {@link IChange}
	 * @return <code>this</code>, for convenience
	 */
	public ChangeCommand setChange(IChangeDescriptor change) {
		this.change = change;
		return this;
	}

	/**
	 * Gets the object representing this command's context.
	 * @return an {@link Object}
	 */
	public Resolver getContext() {
		return resolver;
	}
	
	public void setContext(Resolver resolver) {
		this.resolver = resolver;
	}
	
	/**
	 * Prepares this command for execution once all of its parameters have been
	 * set.
	 * @return <code>this</code>, for convenience
	 */
	public void prepare() {
	}
	
	/**
	 * Indicates whether or not this command's {@link IChange} is ready to be
	 * executed.
	 * @return <code>true</code> if this {@link IChangeCommand} is ready to be
	 * executed, or <code>false</code> otherwise
	 */
	@Override
	public final boolean canExecute() {
		IChangeDescriptor change = getChange();
		if (change instanceof IChangeDescriptor.Group &&
				((IChangeDescriptor.Group)change).size() == 0)
			return false;
		boolean status = false;
		try {
			DescriptorExecutorManager.getInstance().tryValidateChange(
					getContext(), change);
			status = true;
		} catch (ChangeCreationException cre) {
			UI.getActiveStatusLine().setErrorMessage(cre.getRationale());
		}
		return status;
	}
	
	/**
	 * Applies this command's {@link IChange}.
	 */
	@Override
	public final void execute() {
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(
					getContext(), getChange());
			UI.getActiveStatusLine().setErrorMessage(null);
		} catch (ChangeCreationException cre) {
			throw new RuntimeException("BUG: " + this + ".execute() called, " +
					"but the change was rejected", cre);
		}
	}
	
	private IChangeDescriptor inverse = null;
	
	protected IChangeDescriptor getInverse() {
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
			DescriptorExecutorManager.getInstance().tryApplyChange(
					getContext(), getInverse());
		} catch (ChangeCreationException cre) {
			/* do nothing */
		}
	}
	
	/**
	 * Re-applies this command's {@link IChange}.
	 */
	@Override
	public final void redo() {
		execute();
	}
	
	@Override
	public void dispose() {
		if (change instanceof ChangeDescriptorGroup)
			((ChangeDescriptorGroup)change).clear();
		if (inverse instanceof ChangeDescriptorGroup)
			((ChangeDescriptorGroup)inverse).clear();
		change = inverse = null;
	}
	
	@Override
	public String toString() {
		return "ChangeCommand(" + getContext() + ", " + getChange() + ")";
	}
	
	@Override
	public String getLabel() {
		return toString();
	}
}
