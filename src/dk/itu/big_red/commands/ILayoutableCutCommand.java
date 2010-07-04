package dk.itu.big_red.commands;

import java.util.HashMap;
import java.util.Iterator;

import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;


public class ILayoutableCutCommand extends ILayoutableCopyCommand {
	private HashMap<ILayoutable, ILayoutable> parents =
		new HashMap<ILayoutable, ILayoutable>();
	
	public void execute() {
		if (canExecute()) {
			super.execute();
			redo();
		}
	}
	
	public boolean canUndo() {
		System.out.println("canUndo called on " + this);
		return parents.size() != 0;
	}
	
	public void redo() {
		Iterator<ILayoutable> it = list.iterator();
		while (it.hasNext()) {
			ILayoutable n = it.next();
			parents.put(n, (ILayoutable)n.getParent());
			((ILayoutable)n.getParent()).removeChild(n);
		}
	}
	
	public void undo() {
		/*
		 * Notice that the clipboard *is not cleared* when you undo a Cut
		 * operation; this seems to be the behaviour preferred by every
		 * other program in the universe. No sense in diverging, eh?
		 */
		Iterator<ILayoutable> it = list.iterator();
		while (it.hasNext()) {
			ILayoutable n = it.next();
			parents.get(n).addChild(n);
		}
	}
}
