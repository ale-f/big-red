package dk.itu.big_red.editors.bigraph.parts.place;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.utilities.ui.UI;

public class BigraphPlaceTreePart extends AbstractTreePart {
	@Override
	protected List<Layoutable> getModelChildren() {
		return ((Bigraph)getModel()).getChildren();
	}

	@Override
	protected void createEditPolicies() {
	}

	@Override
	protected ImageDescriptor getImageDescriptor() {
		return UI.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
