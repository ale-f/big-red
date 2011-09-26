package dk.itu.big_red.editors.bigraph.commands;

import java.util.HashMap;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;

public class LayoutableCutCommand extends LayoutableCopyCommand {
	private HashMap<Layoutable, Container> parents =
		new HashMap<Layoutable, Container>();
	
	@Override
	public void execute() {
		if (canExecute()) {
			super.execute();
			redo();
		}
	}
	
	@Override
	public boolean canUndo() {
		return parents.size() != 0;
	}
	
	@Override
	public void redo() {
		for (Layoutable n : list) {
			parents.put(n, n.getParent());
			n.getParent().removeChild(n);
		}
	}
	
	@Override
	public void undo() {
		/*
		 * Notice that the clipboard *is not cleared* when you undo a Cut
		 * operation; this seems to be the behaviour preferred by every
		 * other program in the universe. No sense in diverging, eh?
		 */
		for (Layoutable n : list)
			parents.get(n).addChild(n);
	}
}
