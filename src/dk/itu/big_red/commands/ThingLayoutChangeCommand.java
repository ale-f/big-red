package dk.itu.big_red.commands;



import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Thing;



public class ThingLayoutChangeCommand extends Command {
	private Thing model;
	private Rectangle layout, oldLayout;
	
	public void execute() {
		model.setLayout(layout);
	}
	
	public void setConstraint(Rectangle rect) {
		this.layout = rect;
	}

	public void setModel(Object model) {
		this.model = (Thing)model;
		this.oldLayout = this.model.getLayout();
	}

	public void undo() {
		this.model.setLayout(this.oldLayout);
	}
}
