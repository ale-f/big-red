package dk.itu.big_red.part.place;

import java.util.List;



import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editpolicies.ILayoutableDeletePolicy;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.part.AbstractTreePart;
import dk.itu.big_red.util.Utility;

public class RootPlaceTreePart extends AbstractTreePart {
	@Override
	protected List<ILayoutable> getModelChildren() {
		return ((Root)getModel()).getChildren();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ILayoutableDeletePolicy());
	}
	
	@Override
	public String getText() {
		return Integer.toString(((Root)getModel()).getNumber());
	}
	
	@Override
	public Image getImage() {
		return Utility.getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
