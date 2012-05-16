package dk.itu.big_red.editors.bigraph;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableCreateCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableRelayoutCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableReparentCommand;
import dk.itu.big_red.editors.bigraph.parts.BigraphPart;
import dk.itu.big_red.editors.bigraph.parts.EdgePart;
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
			command.setLayout(constraint);
			command.prepare();
		}
		return command;
	}
	
	@Override
	protected Command createAddCommand(
			ChangeBoundsRequest cbr, EditPart child, Object constraint) {
		return createReparentCommand(cbr, child, constraint);
	}
	
	protected Command createReparentCommand(
			ChangeBoundsRequest cbr, EditPart child, Object constraint) {
		ChangeCommand cmd;
		if (!(child instanceof EdgePart)) {
			LayoutableReparentCommand cmd2 = new LayoutableReparentCommand();
			cmd2.setChild(child.getModel());
			cmd2.setParent(getHost().getModel());
			cmd2.setConstraint(constraint);
			cmd = cmd2;
		} else {
			Rectangle layout = (Rectangle)constraint;
			Layoutable self = (Layoutable)getHost().getModel();
			LayoutableRelayoutCommand cmd2 = new LayoutableRelayoutCommand();
			cmd2.setModel(child.getModel());
			cmd2.setLayout(
					layout.translate(self.getRootLayout().getTopLeft()));
			cmd = cmd2;
		}
		return cmd.prepare();
	}
	
	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new RedResizableEditPolicy();
	}
	
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Object requestObject = request.getNewObject();
		
		requestObject.getClass();
		Dimension size = new Dimension(100, 100);
		Layoutable self = (Layoutable)getHost().getModel();
		if (!(self instanceof Container)) {
			return null;
		} else {
			size.setSize(ExtendedDataUtilities.getLayout(((Layoutable)requestObject)).getSize());
		}
		
		LayoutableCreateCommand cmd = new LayoutableCreateCommand();
		cmd.setContainer(self);
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
