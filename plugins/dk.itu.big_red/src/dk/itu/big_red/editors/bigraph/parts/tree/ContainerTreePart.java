package dk.itu.big_red.editors.bigraph.parts.tree;

import java.util.List;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;

public abstract class ContainerTreePart extends AbstractTreePart {
	@Override
	public Container getModel() {
		return (Container)super.getModel();
	}
	
	@Override
	protected List<Layoutable> getModelChildren() {
		return getModel().getChildren();
	}
}
