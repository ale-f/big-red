package dk.itu.big_red.editors.bigraph.parts.place;

import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.parts.AbstractTreePart;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.util.ui.UI;

public class RootPlaceTreePart extends AbstractTreePart {
	@Override
	protected List<Layoutable> getModelChildren() {
		return ((Root)getModel()).getChildren();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}
	
	@Override
	public String getText() {
		return ((Root)getModel()).getName();
	}
	
	@Override
	public Image getImage() {
		return UI.getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
