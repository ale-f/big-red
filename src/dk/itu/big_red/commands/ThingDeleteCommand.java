package dk.itu.big_red.commands;



import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Thing;



public class ThingDeleteCommand extends Command {
	private Thing model;
	private Thing parentModel;
	
	public void execute() {
		this.parentModel.removeChild(model);
	}
	
	public void setModel(Object model) {
		this.model = (Thing)model;
	}
	
	public void setParentModel(Object model) {
		this.parentModel = (Thing)model;
	}
	
	public void undo() {
		this.parentModel.addChild(model);
	}
}
