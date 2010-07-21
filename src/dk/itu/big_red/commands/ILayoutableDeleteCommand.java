package dk.itu.big_red.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.interfaces.ILayoutable;

public class ILayoutableDeleteCommand extends Command {
	private ILayoutable model = null;
	private ILayoutable parentModel = null;
	
	public void execute() {
		this.parentModel.removeChild(model);
	}
	
	public void setModel(Object model) {
		if (model instanceof ILayoutable)
			this.model = (ILayoutable)model;
	}
	
	public void setParentModel(Object model) {
		if (model instanceof ILayoutable)
			this.parentModel = (ILayoutable)model;
	}
	
	public void undo() {
		this.parentModel.addChild(model);
	}
}
