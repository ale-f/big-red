package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Node;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.bigraph.parts.ContainerPart;
import dk.itu.big_red.model.LayoutUtilities;

public class LayoutableCreateCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableCreateCommand() {
		setChange(cg);
	}
	
	private Rectangle layout = null;
	private ContainerPart containerPart = null;
	private Layoutable child = null;
	
	@Override
	public void prepare() {
		cg.clear();
		if (layout == null || containerPart == null || child == null)
			return;
		
		Container container = containerPart.getModel();
		setContext(container.getBigraph());
		String name = container.getBigraph().getFirstUnusedName(child);
		
		if (child instanceof Node){
			if (layout.width < 30) {layout.width=30;}
			if (layout.height< 30) {layout.height=30;}
		}
		
		cg.add(container.changeAddChild(child, name));
		cg.add(new BoundDescriptor(container.getBigraph(),
				new LayoutUtilities.ChangeLayoutDescriptor(
						child.getIdentifier().getRenamed(name),
						null, layout)));
	}
	
	public void setChild(Object s) {
		if (s instanceof Layoutable)
			child = (Layoutable)s;
	}
	
	public void setContainerPart(Object e) {
		if (e instanceof ContainerPart)
			containerPart = (ContainerPart)e;
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
