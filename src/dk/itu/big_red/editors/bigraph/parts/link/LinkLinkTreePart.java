package dk.itu.big_red.editors.bigraph.parts.link;

import org.eclipse.jface.resource.ImageDescriptor;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.bigraph.parts.place.AbstractTreePart;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;

public class LinkLinkTreePart extends AbstractTreePart {
	@Override
	public String getText() {
		return ((Link)getModel()).getName();
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		Object i = getModel();
		String path = null;
		if (i instanceof Edge) {
			path = "resources/icons/bigraph-palette/edge.png";
		} else /* if (i instanceof OuterName) */ {
			path = "resources/icons/bigraph-palette/outer.png";
		}
		return RedPlugin.getImageDescriptor(path);
	}
}
