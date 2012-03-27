package it.uniud.bigredit.command;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.changes.ChangeGroup;


public class LayoutableCreateCommand extends ChangeCommand {
	ChangeGroup cg = new ChangeGroup();
	
	public LayoutableCreateCommand() {
		setChange(cg);
	}
	
	private Rectangle layout = null;
	private Container container = null;
	private Layoutable node = null;
	
	@Override
	public LayoutableCreateCommand prepare() {
		cg.clear();
		if (layout == null || container == null || node == null)
			return this;
		setTarget(container.getBigraph());
		for (Layoutable i : container.getChildren()) {
			if (i instanceof Edge)
				continue;
			else if (i.getLayout().intersects(layout))
				return this;
		}
		if (container instanceof Bigraph) {
			Bigraph bigraph = (Bigraph)container;
			int top = layout.y(),
			    bottom = layout.y() + layout.height();
			if (node instanceof OuterName) {
				if (bottom > bigraph.getLowerOuterNameBoundary())
					return this;
			} else if (node instanceof Root) {
				if (top < bigraph.getUpperRootBoundary() ||
						bottom > bigraph.getLowerRootBoundary())
					return this;
			} else if (node instanceof InnerName) {
				if (top < bigraph.getUpperInnerNameBoundary())
					return this;
			}
		}
		
		String name = container.getBigraph().getFirstUnusedName(node);
		cg.add(container.changeAddChild(node, name),
			node.changeLayout(layout));
		return this;
	}
	
	public void setObject(Object s) {
		if (s instanceof Layoutable)
			node = (Layoutable)s;
	}
	
	public void setContainer(Object e) {
		if (e instanceof Container)
			container = (Container)e;
	}
	
	public void setLayout(Object r) {
		if (r instanceof Rectangle)
			layout = (Rectangle)r;
		else if (r instanceof org.eclipse.draw2d.geometry.Rectangle)
			layout = new Rectangle((org.eclipse.draw2d.geometry.Rectangle)r);
	}
}
