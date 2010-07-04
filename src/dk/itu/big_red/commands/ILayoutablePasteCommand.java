package dk.itu.big_red.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;



import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;



public class ILayoutablePasteCommand extends Command {
	private HashMap<ILayoutable, ILayoutable> list =
		new HashMap<ILayoutable, ILayoutable>();
	private ILayoutable newParent;
	
	public ILayoutablePasteCommand() {
		
	}
	
	public ILayoutablePasteCommand(ILayoutable newParent) {
		this.newParent = newParent;
	}
	
	public ILayoutable getNewParent() {
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
		ArrayList<ILayoutable> bList =
			(ArrayList<ILayoutable>)Clipboard.getDefault().getContents();
		if (bList == null || bList.isEmpty())
			return false;
		Iterator<ILayoutable> it = bList.iterator();
		while (it.hasNext()) {
			ILayoutable node = (ILayoutable)it.next();
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
		
		Iterator<ILayoutable> it = list.keySet().iterator();
		while (it.hasNext()) {
			ILayoutable node = (Thing)it.next();
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
		Iterator<ILayoutable> it = list.values().iterator();
		while (it.hasNext()) {
			ILayoutable node = it.next();
			if (isPastableNode(node)) {
				newParent.addChild(node);
			}
		}
	}
	
	public void undo() {
		Iterator<ILayoutable> it = list.values().iterator();
		while (it.hasNext()) {
			ILayoutable node = it.next();
			if (isPastableNode(node))
				newParent.removeChild(node);
		}
	}
	
	public boolean isPastableNode(ILayoutable node) {
		return (node instanceof Node || node instanceof Root ||
				node instanceof Site);
	}
}
