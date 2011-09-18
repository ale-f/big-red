package dk.itu.big_red.commands;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.part.EdgePart;

public class ILayoutableOrphanCommand extends Command {
	private Container parent = null;
	private List children = null;
	
	public void setParent(Object parent) {
		if (parent instanceof Container)
			this.parent = (Container)parent;
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
				parent.removeChild((LayoutableModelObject)((EditPart)i).getModel());
	}
	
	@Override
	public void undo() {
		for (Object i : children)
			if (i instanceof EditPart && !(i instanceof EdgePart))
				parent.addChild((LayoutableModelObject)((EditPart)i).getModel());
	}
}
