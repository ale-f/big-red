package it.uniud.bigredit.command;

import it.uniud.bigredit.model.BRS;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.changes.ChangeGroup;


public class LayoutableCreateCommand extends ChangeCommand {
	ChangeGroup cg = new ChangeGroup();
	
	public LayoutableCreateCommand() {
		setChange(cg);
	}
	
	private Rectangle layout = null;
	private ModelObject container = null;
	private ModelObject node = null;
	
	@Override
	public LayoutableCreateCommand prepare() {
		cg.clear();
		if (layout == null || container == null || node == null)
			return this;
		
		//setTarget(container.getBigraph());
		if (container instanceof Container) {
		for (Layoutable i : ((Container)container).getChildren()) {
				if (i instanceof Edge)
					continue;
				else if (i.getLayout().intersects(layout))
					return this;
			}
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
		
		
		if (container instanceof Bigraph) {
			String name = ((Bigraph) container).getBigraph().getFirstUnusedName((Layoutable)node);
			cg.add(((Bigraph) container).changeAddChild(((Layoutable)node), name),
					((Layoutable)node).changeLayout(layout));
		}
		if (container instanceof BRS){
			cg.add(((BRS)container).changeAddChild((ModelObject)node, "B0"),
					((BRS)container).changeLayoutChild((ModelObject)node, "B0"));
			
		}
		
		
		
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
