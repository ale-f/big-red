package dk.itu.big_red.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

public class ILayoutableDeleteCommand extends Command {
	private ILayoutable model = null;
	private ILayoutable parentModel = null;
	
	public void setModel(Object model) {
		if (model instanceof ILayoutable)
			this.model = (ILayoutable)model;
	}
	
	public void setParentModel(Object model) {
		if (model instanceof ILayoutable)
			this.parentModel = (ILayoutable)model;
	}
	
	public void execute() {
		this.parentModel.removeChild(model);
		if (parentModel instanceof Bigraph)
			this.parentModel.getBigraph().updateBoundaries();
	}
	
	public void undo() {
		this.parentModel.addChild(model);
		if (parentModel instanceof Bigraph)
			this.parentModel.getBigraph().updateBoundaries();
	}
}
