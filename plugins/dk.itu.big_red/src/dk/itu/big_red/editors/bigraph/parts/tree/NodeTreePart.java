package dk.itu.big_red.editors.bigraph.parts.tree;

import org.bigraph.model.Node;
import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import dk.itu.big_red.editors.assistants.ControlImageDescriptor;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import org.bigraph.extensions.param.ParameterUtilities;

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
	protected String getText() {
		Node model = getModel();
		
		String text = model.getControl().getName() + " " + model.getName();
		String parameter = ParameterUtilities.getParameter(model);
		if (parameter != null) {
			return parameter + " : " + text;
		} else return text;
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return new ControlImageDescriptor(getModel().getControl(), 16, 16);
	}
}
