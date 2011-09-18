package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;

public class LayoutableCopyCommand extends Command {
	protected ArrayList<LayoutableModelObject> list =
		new ArrayList<LayoutableModelObject>();
	
	public boolean addElement(Object node) {
		if (node instanceof LayoutableModelObject)
			return (list.contains(node) ? false : list.add((LayoutableModelObject)node));
		else return false;
	}
	
	private void crunchList() {
		/*
		 * Copying children *and* parents is redundant (the paste operation
		 * performs a deep copy), so crunchList throws away any of the nodes
		 * whose parent is also a candidate for copying.
		 */
		ArrayList<LayoutableModelObject> crunchedList =
			new ArrayList<LayoutableModelObject>();
		for (LayoutableModelObject copycdt : list) {
			if (!list.contains(copycdt.getParent()))
				crunchedList.add(copycdt);
		}
		list = crunchedList;
	}
	
	@Override
	public boolean canExecute() {
		if (list == null || list.isEmpty())
			return false;
		crunchList();
		for (LayoutableModelObject i : list) {
			if (!isCopyableNode(i))
				return false;
		}
		return true;
	}
	
	@Override
	public void execute() {
		if (canExecute())
			Clipboard.getDefault().setContents(list);
	}
	
	@Override
	public boolean canUndo() {
		return true;
	}
	
	public boolean isCopyableNode(Object node) {
		return (node instanceof Root || node instanceof Site || 
				node instanceof Node);
	}
}
