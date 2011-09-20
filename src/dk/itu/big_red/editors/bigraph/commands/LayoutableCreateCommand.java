package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeAddChild;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeLayout;

public class LayoutableCreateCommand extends ChangeCommand {
	ChangeGroup cg = new ChangeGroup();
	
	public LayoutableCreateCommand() {
		setChange(cg);
	}
	
	private Rectangle layout = null;
	private Container container = null;
	private LayoutableModelObject node = null;
	
	private void prepareGroup() {
		cg.clear();
		if (layout == null || container == null || node == null)
			return;
		for (LayoutableModelObject i : container.getChildren()) {
			if (i instanceof Edge)
				continue;
			else if (i.getLayout().intersects(layout))
				return;
		}
		if (container instanceof Bigraph) {
			Bigraph bigraph = (Bigraph)container;
			int top = layout.y,
			    bottom = layout.y + layout.height;
			if (node instanceof OuterName) {
				if (bottom > bigraph.getLowerOuterNameBoundary())
					return;
			} else if (node instanceof Root) {
				if (top < bigraph.getUpperRootBoundary() ||
						bottom > bigraph.getLowerRootBoundary())
					return;
			} else if (node instanceof InnerName) {
				if (top < bigraph.getUpperInnerNameBoundary())
					return;
			}
		}
		cg.add(new BigraphChangeAddChild(container, node),
				new BigraphChangeLayout(node, layout));
	}
	
	public void setObject(Object s) {
		if (s instanceof LayoutableModelObject)
			node = (LayoutableModelObject)s;
		prepareGroup();
	}
	
	public void setContainer(Object e) {
		if (e instanceof Container) {
			container = (Container)e;
			setTarget(container.getBigraph());
		}
		prepareGroup();
	}
	
	public void setLayout(Object r) {
		if (r instanceof Rectangle)
			layout = (Rectangle)r;
		prepareGroup();
	}
}
