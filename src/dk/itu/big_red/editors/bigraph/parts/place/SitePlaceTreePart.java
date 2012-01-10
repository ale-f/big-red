package dk.itu.big_red.editors.bigraph.parts.place;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.editors.bigraph.parts.AbstractTreePart;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.utilities.ui.UI;

public class SitePlaceTreePart extends AbstractTreePart {
	@Override
	protected List<Layoutable> getModelChildren() {
		return new ArrayList<Layoutable>();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
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
