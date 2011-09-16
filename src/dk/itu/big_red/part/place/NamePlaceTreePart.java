package dk.itu.big_red.part.place;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editors.edit_policies.ILayoutableDeletePolicy;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.part.AbstractTreePart;
import dk.itu.big_red.util.Utility;

public class NamePlaceTreePart extends AbstractTreePart {
	@Override
	protected List<Container> getModelChildren() {
		return new ArrayList<Container>();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ILayoutableDeletePolicy());
	}
	
	@Override
	public String getText() {
		return ((InnerName)getModel()).getName();
	}
	
	@Override
	public Image getImage() {
		return Utility.getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
}