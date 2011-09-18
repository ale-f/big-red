package dk.itu.big_red.part.place;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editors.edit_policies.ILayoutableDeletePolicy;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.part.AbstractTreePart;
import dk.itu.big_red.util.UI;

public class SitePlaceTreePart extends AbstractTreePart {
	@Override
	protected List<ILayoutable> getModelChildren() {
		return new ArrayList<ILayoutable>();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ILayoutableDeletePolicy());
	}
	
	@Override
	public String getText() {
		return ((Site)getModel()).getName();
	}
	
	@Override
	public Image getImage() {
		return UI.getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
