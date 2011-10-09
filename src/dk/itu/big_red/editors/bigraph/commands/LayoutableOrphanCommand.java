package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;
import java.util.List;

import dk.itu.big_red.editors.bigraph.parts.AbstractPart;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.ChangeGroup;

public class LayoutableOrphanCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableOrphanCommand() {
		setChange(cg);
	}
	
	private Container parent = null;
	private ArrayList<Layoutable> children =
			new ArrayList<Layoutable>();
	
	public void setParent(Object parent) {
		if (parent instanceof Container)
			this.parent = (Container)parent;
	}
	
	@SuppressWarnings("rawtypes")
	public void setChildren(Object children) {
		if (children instanceof List) {
			List list = (List)children;
			this.children.clear();
			for (Object i : list) {
				if (i instanceof AbstractPart)
					this.children.add(((AbstractPart)i).getModel());
			}
		}
	}
	
	@Override
	public void prepare() {
		cg.clear();
		if (parent != null && children.size() != 0) {
			setTarget(parent.getBigraph());
			for (Layoutable i : children)
				cg.add(parent.changeRemoveChild(i));
		}
	}
}
