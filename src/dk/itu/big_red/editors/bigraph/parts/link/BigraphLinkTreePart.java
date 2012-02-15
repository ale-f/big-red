package dk.itu.big_red.editors.bigraph.parts.link;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editors.bigraph.parts.place.AbstractTreePart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.utilities.Lists;
import dk.itu.big_red.utilities.ui.UI;

public class BigraphLinkTreePart extends AbstractTreePart {
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	protected List<Link> getModelChildren() {
		return Lists.only(getModel().getChildren(), Link.class);
	}
	
	@Override
	protected ImageDescriptor getImageDescriptor() {
		return UI.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
