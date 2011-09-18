package dk.itu.big_red.part.place;

import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editors.edit_policies.ILayoutableDeletePolicy;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.part.AbstractTreePart;
import dk.itu.big_red.util.UI;

public class NodePlaceTreePart extends AbstractTreePart {
	@Override
	protected List<LayoutableModelObject> getModelChildren() {
		return ((Node)getModel()).getChildren();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ILayoutableDeletePolicy());
	}
	
	@Override
	public String getText() {
		return ((Node)getModel()).getControl().getLabel();
	}
	
	@Override
	public Image getImage() {
		return UI.getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
