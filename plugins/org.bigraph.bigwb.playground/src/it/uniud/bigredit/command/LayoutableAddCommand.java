package it.uniud.bigredit.command;

import org.bigraph.model.changes.ChangeGroup;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;


public class LayoutableAddCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableAddCommand() {
		setChange(cg);
	}
	
	private Container parent = null;
	private Layoutable child = null;
	private Rectangle constraint = null;

	@Override
	public LayoutableAddCommand prepare() {
		cg.clear();
		if (parent != null && child != null && constraint != null) {
			setTarget(parent.getBigraph());
			
			if (!(child instanceof Edge)) {
				for (Layoutable i : parent.getChildren()) {
					if (ExtendedDataUtilities.getLayout(i).intersects(constraint))
						return this;
				}
				if (!ExtendedDataUtilities.getLayout(parent).getCopy().setLocation(0, 0).contains(constraint))
					return this;
			}
			
			Rectangle nr = constraint;
			
			if (child instanceof Edge) {
				nr.translate(ExtendedDataUtilities.getRootLayout(parent).getTopLeft());
				parent = parent.getBigraph();
			}
			
			cg.add(parent.changeAddChild(child, child.getName()),
					ExtendedDataUtilities.changeLayout(child, nr));
		}
		return this;
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
