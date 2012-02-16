package dk.itu.big_red.editors.bigraph;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.editors.bigraph.commands.LayoutableAddCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableCreateCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableOrphanCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableRelayoutCommand;
import dk.itu.big_red.editors.bigraph.parts.BigraphPart;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;

public class LayoutableLayoutPolicy extends XYLayoutEditPolicy {
	
	@Override
	protected Command createChangeConstraintCommand(
			ChangeBoundsRequest cbr, EditPart child, Object constraint) {
		LayoutableRelayoutCommand command = null;
		if (!(child instanceof BigraphPart)) {
			command = new LayoutableRelayoutCommand();
			
			command.setModel(child.getModel());
			command.setConstraint(constraint);
			command.prepare();
		}
		return command;
	}
	
	@Override
	protected Command createAddCommand(
			ChangeBoundsRequest cbr, EditPart child, Object constraint) {
		LayoutableAddCommand command = new LayoutableAddCommand();
		command.setParent(getHost().getModel());
		command.setChild(child.getModel());
		command.setConstraint(constraint);
		command.prepare();
		return command;
	}
	
	@Override
	protected Command getOrphanChildrenCommand(Request request) {
		LayoutableOrphanCommand command = null;
		if (request.getType() == REQ_ORPHAN_CHILDREN && request instanceof GroupRequest) {
			GroupRequest groupRequest = (GroupRequest)request;
			command = new LayoutableOrphanCommand();
			
			Object model =
				((EditPart)groupRequest.getEditParts().get(0)).getModel();
			if (model instanceof Layoutable)
				command.setParent(((Layoutable)model).getParent());
			
			command.setChildren(groupRequest.getEditParts());
			command.prepare();
		}
		return command;
	}
	
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Object requestObject = request.getNewObject();
		
		requestObject.getClass();
		Dimension size = new Dimension(100, 100);
		Layoutable parent = (Layoutable)getHost().getModel();
		if (!(parent instanceof Container)) {
			return null;
		} else {
			size.setSize(((Layoutable)requestObject).getLayout().getSize());
		}
		
		LayoutableCreateCommand cmd = new LayoutableCreateCommand();
		cmd.setContainer(getHost().getModel());
		cmd.setObject(request.getNewObject());
		
		Rectangle constraint = (Rectangle)getConstraintFor(request);
		constraint.x = (constraint.x < 0 ? 0 : constraint.x);
		constraint.y = (constraint.y < 0 ? 0 : constraint.y);
		constraint.width = (constraint.width <= 0 ? size.width : constraint.width);
		constraint.height = (constraint.height <= 0 ? size.height : constraint.height);
		cmd.setLayout(constraint);
		cmd.prepare();
		
		return cmd;
	}

}
