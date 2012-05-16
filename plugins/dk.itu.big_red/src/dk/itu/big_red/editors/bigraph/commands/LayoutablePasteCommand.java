package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;
import org.eclipse.gef.ui.actions.Clipboard;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.PropertyScratchpad;
import dk.itu.big_red.model.changes.ChangeGroup;

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
	
	private PropertyScratchpad scratch = null;
	
	@Override
	public LayoutablePasteCommand prepare() {
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
			return this;
		
		setTarget(newParent.getBigraph());
		if (scratch != null) {
			scratch.clear();
		} else scratch = new PropertyScratchpad();
		
		ArrayList<?> bList;
		try {
			bList = (ArrayList<?>)Clipboard.getDefault().getContents();
			if (bList == null)
				return this;
		} catch (Exception e) {
			return this;
		}
		
		for (Object i_ : bList) {
			if (!(i_ instanceof Layoutable))
				continue;
			Layoutable i = (Layoutable)i_;
			
			if (!newParent.canContain(i)) {
				cg.clear();
				return this;
			} else if (i instanceof Node || i instanceof Root ||
					i instanceof Site) {
				Layoutable j = i.clone(null);
				
				String name = newParent.getBigraph().
						getNamespace(Bigraph.getNSI(j)).getNextName(scratch);
				cg.add(newParent.changeAddChild(j, name),
						ExtendedDataUtilities.changeLayout(j, ExtendedDataUtilities.getLayout(j).getCopy().translate(20, 20)));
				
				newParent.addChild(scratch, j, name);
			}
		}
		return this;
	}
}
