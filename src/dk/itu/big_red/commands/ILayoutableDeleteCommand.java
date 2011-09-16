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
			parentModel = (ILayoutable)model;
	}
	
	@Override
	public void execute() {
		parentModel.removeChild(model);
		if (parentModel instanceof Bigraph)
			parentModel.getBigraph().updateBoundaries();
	}
	
	@Override
	public void undo() {
		parentModel.addChild(model);
		if (parentModel instanceof Bigraph)
			parentModel.getBigraph().updateBoundaries();
	}
}
