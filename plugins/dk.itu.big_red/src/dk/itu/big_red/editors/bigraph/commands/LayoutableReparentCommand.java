package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.bigraph.model.assistants.BigraphOperations;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.LayoutUtilities;

public class LayoutableReparentCommand extends ChangeCommand {
	private ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
	
	public LayoutableReparentCommand() {
		setChange(cg);
	}
	
	@Override
	public ChangeDescriptorGroup getChange() {
		return (ChangeDescriptorGroup)super.getChange();
	}
	
	private Container parent = null;
	private Layoutable child = null;
	private Rectangle constraint = null;
	
	public void setParent(Object parent) {
		if (parent instanceof Container)
			this.parent = (Container)parent;
	}
	
	public void setChild(Object child) {
		if (child instanceof Layoutable)
			this.child = (Layoutable)child;
	}
	
	public void setConstraint(Object constraint) {
		if (constraint instanceof Rectangle)
			this.constraint = (Rectangle)constraint;
	}
	
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	@Override
	public void prepare() {
		cg.clear(); scratch.clear();
		if (parent == null || child == null || constraint == null)
			return;
		setContext(parent.getBigraph());
		
		Layoutable.Identifier lid = child.getIdentifier();
		
		BigraphOperations.reparentObject(cg, scratch, child, parent);		
		/* The old reference to child is no longer helpful */
		cg.add(new LayoutUtilities.ChangeLayoutDescriptor(scratch,
				lid.lookup(scratch, parent.getBigraph()), constraint));
	}
}
