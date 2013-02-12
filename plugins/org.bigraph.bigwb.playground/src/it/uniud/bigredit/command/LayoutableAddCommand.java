package it.uniud.bigredit.command;

import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.model.LayoutUtilities;


public class LayoutableAddCommand extends ChangeCommand {
	private ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
	
	public LayoutableAddCommand() {
		setChange(cg);
	}
	
	private Container parent = null;
	private Layoutable child = null;
	private Rectangle constraint = null;

	@Override
	public void prepare() {
		cg.clear();
		if (parent != null && child != null && constraint != null) {
			setContext(parent.getBigraph());
			
			if (!(child instanceof Edge)) {
				for (Layoutable i : parent.getChildren()) {
					if (LayoutUtilities.getLayout(i).intersects(constraint))
						return;
				}
				if (!LayoutUtilities.getLayout(parent).getCopy().setLocation(0, 0).contains(constraint))
					return;
			}
			
			Rectangle nr = constraint;
			
			if (child instanceof Edge) {
				nr.translate(LayoutUtilities.getRootLayout(parent).getTopLeft());
				parent = parent.getBigraph();
			}
			
			cg.add(parent.changeAddChild(child, child.getName()));
			cg.add(new BoundDescriptor(parent.getBigraph(),
					new LayoutUtilities.ChangeLayoutDescriptor(
							null, child, nr)));
		}
	}
	
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
		else if (constraint instanceof org.eclipse.draw2d.geometry.Rectangle)
			this.constraint = new Rectangle((org.eclipse.draw2d.geometry.Rectangle)constraint);
	}
}
