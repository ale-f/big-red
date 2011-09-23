package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.assistants.LinkConnection;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeDisconnect;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeRemoveChild;

public class ModelDeleteCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public ModelDeleteCommand() {
		setChange(cg);
	}
	
	private ArrayList<ModelObject> objects = new ArrayList<ModelObject>();
	
	public static final String GROUP_MAP_ID =
		"dk.itu.big_red.editors.bigraph.commands.ModelDeleteCommand";
	
	public void addObject(ModelObject m) {
		if (m != null)
			objects.add(m);
	}
	
	private void remove(ModelObject m) {
		if (m instanceof LinkConnection) {
			LinkConnection l = (LinkConnection)m;
			setTarget(l.getLink().getBigraph());
			cg.add(new BigraphChangeDisconnect(l.getPoint(), l.getLink()));
			/* XXX! */
		} else if (m instanceof LayoutableModelObject) {
			LayoutableModelObject n = (LayoutableModelObject)m;
			setTarget(n.getBigraph());
			if (n instanceof Container) {
				Container c = (Container)n;
				iterativelyRemoveConnections(c);
			} else if (n instanceof Link) {
				Link l = (Link)n;
				for (Point p : l.getPoints())
					cg.add(new BigraphChangeDisconnect(p, l));
			} else if (n instanceof Point) {
				Point p = (Point)n;
				if (p.getLink() != null)
					cg.add(new BigraphChangeDisconnect(p, p.getLink()));
			}
			cg.add(new BigraphChangeRemoveChild(n.getParent(), n));
		}
	}
	
	private void iterativelyRemoveConnections(Container c) {
		if (c instanceof Node) {
			Node j = (Node)c;
			for (Point p : j.getPorts()) {
				Link l = p.getLink();
				if (l != null && !objects.contains(l))
					cg.add(new BigraphChangeDisconnect(p, l));
			}
		}
		for (LayoutableModelObject i : c.getChildren()) {
			if (i instanceof Container)
				iterativelyRemoveConnections((Container)i);
		}
	}
	
	private boolean parentScheduledForDeletion(LayoutableModelObject i) {
		Container parent = i.getParent();
		while (parent != null) {
			if (objects.contains(parent))
				return true;
			parent = parent.getParent();
		}
		return false;
	}
	
	private boolean linkOrPointScheduledForDeletion(LinkConnection l) {
		if (objects.contains(l.getLink()) ||
			objects.contains(l.getPoint())) {
			return true;
		} else if (l.getPoint() instanceof Port) {
			return parentScheduledForDeletion(l.getPoint());
		}
		return false;
	}

	private boolean allIsWell(LayoutableModelObject m) {
		return m.getBigraph() != null;
	}
	
	@Override
	public void prepare() {
		cg.clear();
		for (ModelObject m : objects) {
			if ((m instanceof LayoutableModelObject &&
					!parentScheduledForDeletion((LayoutableModelObject)m) &&
					allIsWell((LayoutableModelObject)m)) ||
				(m instanceof LinkConnection &&
					!linkOrPointScheduledForDeletion((LinkConnection)m)))
				remove(m);
		}
	}
}
