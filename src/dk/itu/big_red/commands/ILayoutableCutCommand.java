package dk.itu.big_red.commands;

import java.util.HashMap;
import java.util.Iterator;

import dk.itu.big_red.model.interfaces.internal.ILayoutable;

public class ILayoutableCutCommand extends ILayoutableCopyCommand {
	private HashMap<ILayoutable, ILayoutable> parents =
		new HashMap<ILayoutable, ILayoutable>();
	
	@Override
	public void execute() {
		if (canExecute()) {
			super.execute();
			redo();
		}
	}
	
	@Override
	public boolean canUndo() {
		return parents.size() != 0;
	}
	
	@Override
	public void redo() {
		Iterator<ILayoutable> it = list.iterator();
		while (it.hasNext()) {
			ILayoutable n = it.next();
			parents.put(n, n.getParent());
			n.getParent().removeChild(n);
		}
	}
	
	@Override
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
