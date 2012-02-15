package dk.itu.big_red.editors.bigraph.parts.tree;

import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.model.Node;

public class NodeTreePart extends ContainerTreePart {
	@Override
	public Node getModel() {
		return (Node)super.getModel();
	}
	
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}
	
	@Override
	public String getText() {
		return getModel().getControl().getLabel();
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return RedPlugin.getImageDescriptor("resources/icons/triangle.png");
	}
}
