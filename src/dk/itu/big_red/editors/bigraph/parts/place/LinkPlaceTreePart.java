package dk.itu.big_red.editors.bigraph.parts.place;

import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Link;

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
