package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;

import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.eclipse.gef.ui.actions.Clipboard;

public class LayoutablePasteCommand extends ChangeCommand {
	private ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
	
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
		
		setContext(newParent.getBigraph());
		if (scratch != null) {
			scratch.clear();
		} else scratch = new PropertyScratchpad();
		
		ArrayList<?> bList;
		try {
			bList = (ArrayList<?>)Clipboard.getDefault().getContents();
			if (bList == null)
				return;
		} catch (Exception e) {
			return;
		}
		
		for (Object i_ : bList) {
			if (!(i_ instanceof Layoutable))
				continue;
			/*Layoutable i = (Layoutable)i_;
			
			if (!newParent.canContain(i)) {
				cg.clear();
				return;
			} else if (i instanceof Node || i instanceof Root ||
					i instanceof Site) {
				
				Layoutable j = i.clone(null);
				
				String name = newParent.getBigraph().
						getNamespace(Bigraph.getNSI(j)).getNextName(scratch);
				cg.add(scratch.executeChange(newParent.changeAddChild(j, name)));
				cg.add(LayoutUtilities.changeLayout(j,
										LayoutUtilities.getLayout(j).getCopy().translate(20, 20)));
				
			}*/
		}
	}
}
