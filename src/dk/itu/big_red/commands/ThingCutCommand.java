package dk.itu.big_red.commands;

import java.util.HashMap;
import java.util.Iterator;

import dk.itu.big_red.model.*;


public class ThingCutCommand extends ThingCopyCommand {
	private HashMap<Thing, Thing> parents = new HashMap<Thing, Thing>();
	
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
		Iterator<Thing> it = list.iterator();
		while (it.hasNext()) {
			Thing n = it.next();
			parents.put(n, (Thing)n.getParent());
			((Thing)n.getParent()).removeChild(n);
		}
	}
	
	public void undo() {
		/*
		 * Notice that the clipboard *is not cleared* when you undo a Cut
		 * operation; this seems to be the behaviour preferred by every
		 * other program in the universe. No sense in diverging, eh?
		 */
		Iterator<Thing> it = list.iterator();
		while (it.hasNext()) {
			Thing n = it.next();
			parents.get(n).addChild(n);
		}
	}
	
	public boolean isCopyableNode(Thing node) {
		return (node instanceof Root || node instanceof Site || node instanceof Node);
	}
}
