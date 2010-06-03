package dk.itu.big_red.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;



import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import dk.itu.big_red.model.*;



public class ThingPasteCommand extends Command {
	private HashMap<Thing, Thing> list = new HashMap<Thing, Thing>();
	private Thing newParent;
	
	public ThingPasteCommand() {
		
	}
	
	public ThingPasteCommand(Thing newParent) {
		this.newParent = newParent;
	}
	
	public Thing getNewParent() {
		return newParent;
	}
	
	public void setNewParent(Thing newParent) {
		if (newParent != null)
			this.newParent = newParent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean canExecute() {
		/*
		 * FIXME: If several elements with the same parent are copied and then
		 * pasted *while the copied elements are still selected*, then
		 * PasteNodeAction is designed to provide their mutual parent as
		 * newParent. This is all very good, but it means that, immediately
		 * after copying to the clipboard, Paste will be disabled (the bList
		 * check having failed). How can this be resolved?
		 */
		if (newParent == null)
			return false;
		ArrayList<Thing> bList = (ArrayList<Thing>)Clipboard.getDefault().getContents();
		if (bList == null || bList.isEmpty())
			return false;
		Iterator<Thing> it = bList.iterator();
		while (it.hasNext()) {
			Thing node = (Thing)it.next();
			if (!newParent.canContain(node))
				return false;
			else if (isPastableNode(node))
				list.put(node, null);
		}
		return true;
	}
	
	public void execute() {
		if (!canExecute())
			return;
		
		Iterator<Thing> it = list.keySet().iterator();
		while (it.hasNext()) {
			Thing node = (Thing)it.next();
			try {
				list.put(node, (Thing)node.clone());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		redo();
	}
	
	@Override
	public void redo() {
		Iterator<Thing> it = list.values().iterator();
		while (it.hasNext()) {
			Thing node = it.next();
			if (isPastableNode(node)) {
				newParent.addChild(node);
			}
		}
	}
	
	public void undo() {
		Iterator<Thing> it = list.values().iterator();
		while (it.hasNext()) {
			Thing node = it.next();
			if (isPastableNode(node))
				newParent.removeChild(node);
		}
	}
	
	public boolean isPastableNode(Thing node) {
		return (node instanceof Node || node instanceof Root || node instanceof Site);
	}
}
