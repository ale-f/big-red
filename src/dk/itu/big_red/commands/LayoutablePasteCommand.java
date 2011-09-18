package dk.itu.big_red.commands;

import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;

public class LayoutablePasteCommand extends Command {
	private HashMap<LayoutableModelObject, LayoutableModelObject> list =
		new HashMap<LayoutableModelObject, LayoutableModelObject>();
	private Container newParent;
	
	public LayoutablePasteCommand() {
		
	}
	
	public LayoutablePasteCommand(Object newParent) {
		setNewParent(newParent);
	}
	
	public Container getNewParent() {
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
		ArrayList<LayoutableModelObject> bList =
			(ArrayList<LayoutableModelObject>)Clipboard.getDefault().getContents();
		if (bList == null || bList.isEmpty())
			return false;
		for (LayoutableModelObject node : bList) {
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
		
		for (LayoutableModelObject node : list.keySet()) {
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
		for (LayoutableModelObject node : list.values()) {
			if (isPastableNode(node)) {
				newParent.addChild(node);
			}
		}
	}
	
	@Override
	public void undo() {
		for (LayoutableModelObject node : list.values()) {
			if (isPastableNode(node))
				newParent.removeChild(node);
		}
	}
	
	public boolean isPastableNode(Object node) {
		return (node instanceof Node || node instanceof Root ||
				node instanceof Site);
	}
}
