package dk.itu.big_red.editors.bigraph.parts.tree;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Layoutable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.utilities.ui.UI;

public class BigraphTreePart extends ContainerTreePart {
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}

	@Override
	protected void createEditPolicies() {
	}

	@Override
	protected List<Layoutable> getLinkChildren() {
		ArrayList<Layoutable> l = new ArrayList<Layoutable>();
		l.addAll(getModel().getOuterNames());
		l.addAll(getModel().getEdges());
		return l;
	}
	
	@Override
	protected ImageDescriptor getImageDescriptor() {
		return UI.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
