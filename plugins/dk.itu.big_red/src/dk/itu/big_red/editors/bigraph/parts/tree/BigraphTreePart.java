package dk.itu.big_red.editors.bigraph.parts.tree;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.model.Bigraph;
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
	protected ImageDescriptor getImageDescriptor() {
		return UI.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
