package dk.itu.big_red.editors.bigraph.parts.tree;

import org.bigraph.model.InnerName;
import org.bigraph.model.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;

public class PointTreePart extends AbstractTreePart {
	@Override
	public Point getModel() {
		return (Point)super.getModel();
	}

	@Override
	protected void createEditPolicies() {
		if (getModel() instanceof InnerName)
			installEditPolicy(EditPolicy.COMPONENT_ROLE,
					new LayoutableDeletePolicy());
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return RedPlugin.getImageDescriptor(
				"resources/icons/bigraph-palette/inner.png");
	}
}