package dk.itu.big_red.editors.bigraph.parts.place;

import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.utilities.ui.UI;

public class LinkPlaceTreePart extends AbstractTreePart {
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}
	
	@Override
	public String getText() {
		return ((Link)getModel()).getName();
	}
	
	@Override
	public Image getImage() {
		return UI.getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
