package dk.itu.big_red.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;

public class ILayoutableDeleteCommand extends Command {
	private LayoutableModelObject model = null;
	private Container parentModel = null;
	
	public void setModel(Object model) {
		if (model instanceof LayoutableModelObject)
			this.model = (LayoutableModelObject)model;
	}
	
	public void setParentModel(Object model) {
		if (model instanceof Container)
			parentModel = (Container)model;
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
