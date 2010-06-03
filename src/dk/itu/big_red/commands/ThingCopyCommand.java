package dk.itu.big_red.commands;

import java.util.ArrayList;
import java.util.Iterator;



import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;

public class ThingCopyCommand extends Command {
	protected ArrayList<Thing> list = new ArrayList<Thing>();
	
	public boolean addElement(Thing node) {
		return (list.contains(node) ? false : list.add(node));
	}
	
	private void crunchList() {
		/*
		 * Copying children *and* parents is redundant (the paste operation
		 * performs a deep copy), so crunchList throws away any of the nodes
		 * whose parent is also a candidate for copying.
		 */
		ArrayList<Thing> crunchedList = new ArrayList<Thing>();
		Iterator<Thing> it = list.iterator();
		while (it.hasNext()) {
			Thing copycdt = it.next();
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
		Iterator<Thing> it = list.iterator();
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
	
	public boolean isCopyableNode(Thing node) {
		return (node instanceof Root || node instanceof Site || node instanceof Node);
	}
}
