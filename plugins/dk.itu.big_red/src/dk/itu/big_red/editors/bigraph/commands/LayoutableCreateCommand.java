package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Node;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.bigraph.parts.ContainerPart;
import dk.itu.big_red.model.LayoutUtilities;

public class LayoutableCreateCommand extends ChangeCommand {
	private ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
	
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
		Layoutable.Identifier childID = child.getIdentifier().getRenamed(name);
		
		if (child instanceof Node){
			if (layout.width < 30) {layout.width=30;}
			if (layout.height< 30) {layout.height=30;}
		}
		
		cg.add(new Container.ChangeAddChildDescriptor(
				container.getIdentifier(), childID));
		cg.add(new LayoutUtilities.ChangeLayoutDescriptor(
				childID, null, layout));
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
