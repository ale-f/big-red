package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeAddChild;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeLayout;

public class LayoutableAddCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableAddCommand() {
		setChange(cg);
	}
	
	private Container parent = null;
	private LayoutableModelObject child = null;
	private Rectangle constraint = null;
	
	private void prepareGroup() {
		cg.clear();
		if (parent != null && child != null && constraint != null) {
			setTarget(parent.getBigraph());
			
			if (!(child instanceof Edge)) {
				for (LayoutableModelObject i : parent.getChildren()) {
					if (i.getLayout().intersects(constraint))
						return;
				}
				if (!parent.getLayout().setLocation(0, 0).contains(constraint))
					return;
			}
			
			Rectangle nr = constraint;
			
			if (child instanceof Edge) {
				nr = new Rectangle(constraint).translate(parent.getRootLayout().getTopLeft());
			} else cg.add(new BigraphChangeAddChild(parent, child));
			
			cg.add(new BigraphChangeLayout(child, nr));
		}
	}
	
	public void setParent(Object parent) {
		if (parent instanceof Container)
			this.parent = (Container)parent;
		prepareGroup();
	}
	
	public void setChild(Object child) {
		if (child instanceof LayoutableModelObject)
			this.child = (LayoutableModelObject)child;
		prepareGroup();
	}
	
	public void setConstraint(Object constraint) {
		if (constraint instanceof Rectangle)
			this.constraint = (Rectangle)constraint;
		prepareGroup();
	}
}
