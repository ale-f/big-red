package dk.itu.big_red.editors.bigraph.parts.link;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;

import dk.itu.big_red.editors.bigraph.parts.place.AbstractTreePart;
import dk.itu.big_red.utilities.ui.UI;

public class NodeLinkTreePart extends AbstractTreePart {
	@Override
	protected ImageDescriptor getImageDescriptor() {
		return UI.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
	}
}
