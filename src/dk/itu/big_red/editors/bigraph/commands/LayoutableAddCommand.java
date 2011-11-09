package dk.itu.big_red.editors.bigraph.commands;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.util.geometry.Rectangle;

public class LayoutableAddCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
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
			setTarget(parent.getBigraph());
			
			if (!(child instanceof Edge)) {
				for (Layoutable i : parent.getChildren()) {
					if (i.getLayout().intersects(constraint))
						return;
				}
				if (!parent.getLayout().getCopy().setLocation(0, 0).contains(constraint))
					return;
			}
			
			Rectangle nr = constraint;
			
			if (child instanceof Edge) {
				nr.translate(parent.getRootLayout().getTopLeft());
				parent = parent.getBigraph();
			}
			
			cg.add(parent.changeAddChild(child, child.getName()),
					child.changeLayout(nr));
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
