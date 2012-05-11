package dk.itu.big_red.editors.bigraph.commands;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.changes.ChangeGroup;

public class LayoutableCreateCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableCreateCommand() {
		setChange(cg);
	}
	
	private Rectangle layout = null;
	private Container container = null;
	private Layoutable child = null;
	
	@Override
	public LayoutableCreateCommand prepare() {
		cg.clear();
		if (layout == null || container == null || child == null)
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
			if (child instanceof OuterName) {
				if (bottom > bigraph.getLowerOuterNameBoundary())
					return this;
			} else if (child instanceof Root) {
				if (top < bigraph.getUpperRootBoundary() ||
						bottom > bigraph.getLowerRootBoundary())
					return this;
			} else if (child instanceof InnerName) {
				if (top < bigraph.getUpperInnerNameBoundary())
					return this;
			}
		}
		
		String name = container.getBigraph().getFirstUnusedName(child);
		cg.add(container.changeAddChild(child, name),
			child.changeLayout(layout));
		return this;
	}
	
	public void setObject(Object s) {
		if (s instanceof Layoutable)
			child = (Layoutable)s;
	}
	
	public void setContainer(Object e) {
		if (e instanceof Container)
			container = (Container)e;
	}
	
	public void setLayout(Object r) {
		if (r instanceof Rectangle) {
			layout = (Rectangle)r;
			if (layout.width < 10)
				layout.width = 10;
			if (layout.height < 10)
				layout.height = 10;
		}
	}
}
