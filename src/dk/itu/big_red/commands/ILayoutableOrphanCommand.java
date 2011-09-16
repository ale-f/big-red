package dk.itu.big_red.commands;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.part.EdgePart;

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
	
	@Override
	public boolean canExecute() {
		return (parent != null && children != null);
	}
	
	@Override
	public void execute() {
		for (Object i : children)
			if (i instanceof EditPart && !(i instanceof EdgePart))
				parent.removeChild((ILayoutable)((EditPart)i).getModel());
	}
	
	@Override
	public void undo() {
		for (Object i : children)
			if (i instanceof EditPart && !(i instanceof EdgePart))
				parent.addChild((ILayoutable)((EditPart)i).getModel());
	}
}
