package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.LayoutUtilities;

public class LayoutableReparentCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableReparentCommand() {
		setChange(cg);
	}
	
	@Override
	public ChangeGroup getChange() {
		return (ChangeGroup)super.getChange();
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
	
	private ChangeGroup post = new ChangeGroup();
	
	private void remove(Layoutable l, boolean root) {
		if (l instanceof Container)
			for (Layoutable k : ((Container)l).getChildren())
				remove(k, false);
		
		if (l instanceof Node) {
			for (Port p : ((Node)l).getPorts()) {
				Link li = p.getLink();
				if (li != null) {
					cg.add(new BoundDescriptor(l.getBigraph(),
							new Point.ChangeDisconnectDescriptor(
									p.getIdentifier(), li.getIdentifier())));
					post.add(new BoundDescriptor(l.getBigraph(),
							new Point.ChangeConnectDescriptor(
									p.getIdentifier(), li.getIdentifier())));
				}
			}
		}
		
		cg.add(l.changeRemove());
		if (!root) {
			post.add(0, LayoutUtilities.changeLayout(
					l, LayoutUtilities.getLayout(l)));
			post.add(0, l.getParent().changeAddChild(l, l.getName()));
		}
	}
	
	@Override
	public void prepare() {
		cg.clear(); post.clear();
		if (parent == null || child == null || constraint == null)
			return;
		setTarget(parent.getBigraph());
		
		remove(child, true);
		
		cg.add(parent.changeAddChild(child, child.getName()));
		cg.add(LayoutUtilities.changeLayout(child, constraint));
		cg.add(post);
	}
}
