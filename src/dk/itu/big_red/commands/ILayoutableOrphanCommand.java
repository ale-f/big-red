package dk.itu.big_red.commands;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class ILayoutableOrphanCommand extends Command {
	private ILayoutable parent = null;
	private List children = null;
	
	public void setParent(Object parent) {
		if (parent instanceof ILayoutable)
			this.parent = (ILayoutable)parent;
	}
	
	public void setChildren(Object children) {
		if (children instanceof List)
			this.children = (List)children;
	}
	
	public boolean canExecute() {
		return (this.parent != null && this.children != null);
	}
	
	public void execute() {
		for (Object i : children)
			if (i instanceof EditPart && !(i instanceof Edge))
				parent.removeChild((ILayoutable)((EditPart)i).getModel());
	}
	
	public void undo() {
		for (Object i : children)
			if (i instanceof EditPart && !(i instanceof Edge))
				parent.addChild((ILayoutable)((EditPart)i).getModel());
	}
}
