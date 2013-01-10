package dk.itu.big_red.editors.bigraph.parts.tree;

import java.util.Collection;
import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;

public abstract class ContainerTreePart extends AbstractTreePart {
	@Override
	public Container getModel() {
		return (Container)super.getModel();
	}
	
	@Override
	protected Collection<? extends Layoutable> getPlaceChildren() {
		return getModel().getChildren();
	}
}
