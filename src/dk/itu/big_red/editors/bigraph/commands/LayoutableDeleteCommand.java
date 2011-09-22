package dk.itu.big_red.editors.bigraph.commands;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeDisconnect;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeRemoveChild;

public class LayoutableDeleteCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableDeleteCommand() {
		setChange(cg);
	}
	
	private LayoutableModelObject object = null;
	private Container parent = null;
	
	public void setObject(Object model) {
		if (model instanceof LayoutableModelObject) {
			this.object = (LayoutableModelObject)model;
			this.parent = this.object.getParent();
			if (this.parent == null)
				this.object = null;
		}
		prepare();
	}
	
	private void iterativelyRemoveConnections(Container c) {
		if (c instanceof Node) {
			Node j = (Node)c;
			for (Point p : j.getPorts()) {
				Link l = p.getLink();
				if (l != null)
					cg.add(new BigraphChangeDisconnect(p, l));
			}
		}
		for (LayoutableModelObject i : c.getChildren()) {
			if (i instanceof Container)
				iterativelyRemoveConnections((Container)i);
		}
	}

	@Override
	public void prepare() {
		cg.clear();
		if (this.object == null)
			return;
		setTarget(this.object.getBigraph());
		
		if (this.object instanceof Link) {
			Link l = (Link)this.object;
			for (Point p : l.getPoints())
				cg.add(new BigraphChangeDisconnect(p, l));
		} else if (this.object instanceof Point) {
			Point p = (Point)this.object;
			if (p.getLink() != null)
				cg.add(new BigraphChangeDisconnect(p, p.getLink()));
		} else if (this.object instanceof Container) {
			iterativelyRemoveConnections((Container)this.object);
		}
		cg.add(new BigraphChangeRemoveChild(this.parent, this.object));
	}
}
