package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.IChangeable;

public class ChangeCommand extends Command {
	private Change change;
	private IChangeable target;
	
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
	
	@Override
	public final boolean canExecute() {
		return (change != null && target != null &&
				target.validateChange(change));
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
