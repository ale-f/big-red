package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.IStatusLineManager;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.util.UI;

public abstract class ChangeCommand extends Command {
	private Change change;
	private IChangeable target;
	
	private static IStatusLineManager getActiveStatusLine() {
		return UI.getWorkbenchPage().getActiveEditor().getEditorSite().
				getActionBars().getStatusLineManager();
	}
	
	public Change getChange() {
		return change;
	}

	public void setChange(Change change) {
		this.change = change;
	}

	public IChangeable getTarget() {
		return target;
	}

	public void setTarget(IChangeable target) {
		this.target = target;
	}
	
	public abstract void prepare();
	
	@Override
	public boolean canExecute() {
		boolean status = (change != null && target != null &&
				change.isReady());
		if (status) {
			status = status && target.validateChange(change);
			getActiveStatusLine().setErrorMessage(status ? null :
				target.getLastRejection().getRationale());
		}
		return status;
	}
	
	@Override
	public final void execute() {
		target.applyChange(change);
	}
	
	@Override
	public final void undo() {
		target.applyChange(change.inverse());
	}
	
	@Override
	public final void redo() {
		execute();
	}
}
