package dk.itu.big_red.commands;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class ILayoutableCopyCommand extends Command {
	protected ArrayList<ILayoutable> list =
		new ArrayList<ILayoutable>();
	
	public boolean addElement(Object node) {
		if (node instanceof ILayoutable)
			return (list.contains(node) ? false : list.add((ILayoutable)node));
		else return false;
	}
	
	private void crunchList() {
		/*
		 * Copying children *and* parents is redundant (the paste operation
		 * performs a deep copy), so crunchList throws away any of the nodes
		 * whose parent is also a candidate for copying.
		 */
		ArrayList<ILayoutable> crunchedList =
			new ArrayList<ILayoutable>();
		Iterator<ILayoutable> it = list.iterator();
		while (it.hasNext()) {
			ILayoutable copycdt = it.next();
			if (!list.contains(copycdt.getParent())) {
				crunchedList.add(copycdt);
			}
		}
		list = crunchedList;
	}
	
	public boolean canExecute() {
		if (list == null || list.isEmpty())
			return false;
		crunchList();
		Iterator<ILayoutable> it = list.iterator();
		while (it.hasNext()) {
			if (!isCopyableNode(it.next()))
				return false;
		}
		return true;
	}
	
	public void execute() {
		if (canExecute())
			Clipboard.getDefault().setContents(list);
	}
	
	public boolean canUndo() {
		return true;
	}
	
	public boolean isCopyableNode(Object node) {
		return (node instanceof Root || node instanceof Site || 
				node instanceof Node);
	}
}
