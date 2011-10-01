package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;
import org.eclipse.gef.ui.actions.Clipboard;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeAddChild;

public class LayoutablePasteCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	private Container newParent;
	
	public LayoutablePasteCommand() {
		setChange(cg);
	}
	
	public Container getNewParent() {
		return newParent;
	}
	
	public void setNewParent(Object newParent) {
		if (newParent instanceof Container)
			this.newParent = (Container)newParent;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void prepare() {
		/*
		 * FIXME: If several elements with the same parent are copied and then
		 * pasted *while the copied elements are still selected*, then
		 * PasteNodeAction is designed to provide their mutual parent as
		 * newParent. This is all very good, but it means that, immediately
		 * after copying to the clipboard, Paste will be disabled (the bList
		 * check having failed). How can this be resolved?
		 */
		cg.clear();
		if (newParent == null)
			return;
		setTarget(newParent.getBigraph());
		
		ArrayList<Layoutable> bList;
		try {
			bList = (ArrayList<Layoutable>)Clipboard.getDefault().getContents();
			if (bList == null)
				return;
		} catch (Exception e) {
			return;
		}
		
		for (Layoutable i : bList) {
			if (!newParent.canContain(i)) {
				cg.clear();
				return;
			} else if (i instanceof Node || i instanceof Root ||
					i instanceof Site) {
				Layoutable j = i.clone(null);
				cg.add(new BigraphChangeAddChild(newParent, j, j.getLayout().getCopy().translate(20, 20)));
			}
		}
	}
}
