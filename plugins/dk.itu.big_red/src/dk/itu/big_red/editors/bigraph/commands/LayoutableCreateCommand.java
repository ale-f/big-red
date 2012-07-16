package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.changes.ChangeGroup;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.parts.ContainerPart;

public class LayoutableCreateCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public LayoutableCreateCommand() {
		setChange(cg);
	}
	
	private Rectangle layout = null;
	private ContainerPart containerPart = null;
	private Layoutable child = null;
	
	@Override
	public LayoutableCreateCommand prepare() {
		cg.clear();
		if (layout == null || containerPart == null || child == null)
			return this;
		
		Container container = containerPart.getModel();
		setTarget(container.getBigraph());
		for (Layoutable i : container.getChildren()) {
			if (i instanceof Edge)
				continue;
			else if (ExtendedDataUtilities.getLayout(i).intersects(layout))
				return this;
		}
		
		String name = container.getBigraph().getFirstUnusedName(child);
		cg.add(container.changeAddChild(child, name),
			ExtendedDataUtilities.changeLayout(child, layout));
		return this;
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
