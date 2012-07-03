package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;

import org.bigraph.model.Layoutable;
import org.bigraph.model.Node;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.Clipboard;

public class LayoutableCopyCommand extends Command {
	private ArrayList<Layoutable> list =
		new ArrayList<Layoutable>();
	
	public boolean addElement(Object node) {
		if (node instanceof Layoutable)
			return (list.contains(node) ? false : list.add((Layoutable)node));
		else return false;
	}
	
	private void crunchList() {
		/*
		 * Copying children *and* parents is redundant (the paste operation
		 * performs a deep copy), so crunchList throws away any of the nodes
		 * whose parent is also a candidate for copying.
		 */
		ArrayList<Layoutable> crunchedList =
			new ArrayList<Layoutable>();
		for (Layoutable copycdt : list) {
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
		for (Layoutable i : list) {
			if (!canCopy(i))
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
	
	public static boolean canCopy(Object node) {
		return (node instanceof Root || node instanceof Site || 
				node instanceof Node);
	}
}
