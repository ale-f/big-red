package dk.itu.big_red.commands;



import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.interfaces.ILayoutable;



public class ILayoutableRelayoutCommand extends Command {
	private ILayoutable model;
	private Rectangle layout, oldLayout;
	
	public void execute() {
		model.setLayout(layout);
	}
	
	public void setConstraint(Rectangle rect) {
		this.layout = rect;
	}

	public void setModel(Object model) {
		this.model = (ILayoutable)model;
		this.oldLayout = this.model.getLayout();
	}

	public void undo() {
		model.setLayout(this.oldLayout);
	}
}
