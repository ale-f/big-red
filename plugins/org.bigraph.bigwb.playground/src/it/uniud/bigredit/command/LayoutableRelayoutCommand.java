package it.uniud.bigredit.command;

import it.uniud.bigredit.model.BRS;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.model.LayoutUtilities;
import it.uniud.bigredit.model.Reaction;

public class LayoutableRelayoutCommand extends ChangeCommand {
	private ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
	
	public LayoutableRelayoutCommand() {
		setChange(cg);
	}
	
	private ModelObject model;
	private ModelObject parent;
	private Rectangle layout;
	
	public void setConstraint(Object rect) {
		if (rect instanceof Rectangle)
			layout = (Rectangle)rect;
	}

	public void setModel(Object model) {
		if (model instanceof Layoutable)
			this.model = (Layoutable)model;
		if (model instanceof ModelObject)
			this.model = (ModelObject)model;
	}
	
	@Override
	public void prepare() {
		cg.clear();
		if (model == null || layout == null)
			return;
		
		
		if((model instanceof Bigraph) || (model instanceof Reaction)){
			
			if(parent instanceof BRS){
				cg.add(((BRS)parent).changeLayoutChild(model,layout));
			}else if(parent instanceof Reaction){
				cg.add(((Reaction)parent).changeLayoutChild((Bigraph)model,layout));
			}
		}else{
			setContext(((Layoutable)model).getBigraph());
			if ((model instanceof Edge || noOverlap()) && boundariesSatisfied())
				cg.add(new BoundDescriptor(((Layoutable)model).getBigraph(),
						new LayoutUtilities.ChangeLayoutDescriptor(null,
								(Layoutable)model, layout)));
		}
	}
	
	public boolean noOverlap() {
		for (Layoutable i : ((Layoutable)model).getParent().getChildren()) {
			if (i instanceof Edge || i == model)
				continue;
			else if (LayoutUtilities.getLayout(i).intersects(layout))
				return false;
		}
		return true;
	}
	
	private boolean boundariesSatisfied() {
		if (!(((Layoutable)model).getParent() instanceof Bigraph))
			return true;
		return true;
	}

	public ModelObject getParent() {
		return parent;
	}

	public void setParent(ModelObject parent) {
		this.parent = parent;
	}
}
