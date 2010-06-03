package dk.itu.big_red.part.tree.place;

import java.util.List;



import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editpolicies.ThingDeletePolicy;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.part.tree.AbstractTreePart;
import dk.itu.big_red.util.Utility;

public class NodePlaceTreePart extends AbstractTreePart {
	@Override
	protected List<Thing> getModelChildren() {
		return ((Node)getModel()).getChildrenArray();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ThingDeletePolicy());
	}
	
	@Override
	public String getText() {
		return ((Node)getModel()).getControl().getLabel();
	}
	
	@Override
	public Image getImage() {
		return Utility.getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
