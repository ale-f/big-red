package it.uniud.bigredit.command;

import it.uniud.bigredit.model.BRS;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.InnerName;
import it.uniud.bigredit.model.Reaction;

public class LayoutableRelayoutCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
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
	public LayoutableRelayoutCommand prepare() {
		cg.clear();
		if (model == null || layout == null)
			return this;
		
		
		if((model instanceof Bigraph) || (model instanceof Reaction)){
			System.out.println("instance of bigraph in prepare command");
			setTarget((BRS)parent);
			cg.add(((BRS)parent).changeLayoutChild(model,layout));
		}else{
			setTarget(((Layoutable)model).getBigraph());
			if ((model instanceof Edge || noOverlap()) && boundariesSatisfied())
				cg.add(((Layoutable)model).changeLayout(layout));
		}
		return this;
	}
	
	public boolean noOverlap() {
		for (Layoutable i : ((Layoutable)model).getParent().getChildren()) {
			if (i instanceof Edge || i == model)
				continue;
			else if (i.getLayout().intersects(layout))
				return false;
		}
		return true;
	}
	
	private boolean boundariesSatisfied() {
		if (!(((Layoutable)model).getParent() instanceof Bigraph))
			return true;
		Bigraph bigraph = (Bigraph)((Layoutable)model).getParent();
		int top = layout.y(),
		    bottom = layout.y() + layout.height();
		if (model instanceof OuterName) {
			if (bottom > bigraph.getLowerOuterNameBoundary())
				return false;
		} else if (model instanceof Root) {
			if (top < bigraph.getUpperRootBoundary() ||
					bottom > bigraph.getLowerRootBoundary())
				return false;
		} else if (model instanceof InnerName) {
			if (top < bigraph.getUpperInnerNameBoundary())
				return false;
		}
		return true;
	}

	public ModelObject getParent() {
		return parent;
	}

	public void setParent(ModelObject parent) {
		this.parent = parent;
	}
}
