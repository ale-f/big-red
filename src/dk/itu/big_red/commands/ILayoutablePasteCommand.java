package dk.itu.big_red.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

public class ILayoutablePasteCommand extends Command {
	private HashMap<ILayoutable, ILayoutable> list =
		new HashMap<ILayoutable, ILayoutable>();
	private Container newParent;
	
	public ILayoutablePasteCommand() {
		
	}
	
	public ILayoutablePasteCommand(Object newParent) {
		setNewParent(newParent);
	}
	
	public ILayoutable getNewParent() {
		return newParent;
	}
	
	public void setNewParent(Object newParent) {
		if (newParent instanceof Container)
			this.newParent = (Container)newParent;
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
			ILayoutable node = it.next();
			if (!newParent.canContain(node))
				return false;
			else if (isPastableNode(node))
				list.put(node, null);
		}
		return true;
	}
	
	@Override
	public void execute() {
		if (!canExecute())
			return;
		
		Iterator<ILayoutable> it = list.keySet().iterator();
		while (it.hasNext()) {
			ILayoutable node = it.next();
			try {
				list.put(node, node.clone());
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
	
	@Override
	public void undo() {
		Iterator<ILayoutable> it = list.values().iterator();
		while (it.hasNext()) {
			ILayoutable node = it.next();
			if (isPastableNode(node))
				newParent.removeChild(node);
		}
	}
	
	public boolean isPastableNode(Object node) {
		return (node instanceof Node || node instanceof Root ||
				node instanceof Site);
	}
}
