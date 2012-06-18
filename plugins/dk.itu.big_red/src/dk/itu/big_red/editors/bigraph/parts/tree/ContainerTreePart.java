package dk.itu.big_red.editors.bigraph.parts.tree;

import java.util.List;

import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;


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
