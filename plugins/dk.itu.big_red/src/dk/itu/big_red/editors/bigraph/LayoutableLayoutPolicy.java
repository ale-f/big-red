package dk.itu.big_red.editors.bigraph;

import java.util.List;

import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableCreateCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableMoveCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableReparentCommand;
import dk.itu.big_red.editors.bigraph.parts.EdgePart;

public class LayoutableLayoutPolicy extends XYLayoutEditPolicy {
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
			LayoutableMoveCommand cmd2 = new LayoutableMoveCommand();
			cmd2.addObject(child.getModel());
			cmd2.setMoveDelta(cbr.getMoveDelta());
			cmd = cmd2;
		}
		cmd.prepare();
		return cmd;
	}
	
	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		return new RedResizableEditPolicy();
	}
	
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Layoutable self = (Layoutable)getHost().getModel();
		if (!(self instanceof Container))
			return null;
		
		LayoutableCreateCommand cmd = new LayoutableCreateCommand();
		cmd.setContainerPart(getHost());
		cmd.setChild(request.getNewObject());
		
		Rectangle constraint = (Rectangle)getConstraintFor(request);
		constraint.x = (constraint.x < 0 ? 0 : constraint.x);
		constraint.y = (constraint.y < 0 ? 0 : constraint.y);
		constraint.width = (constraint.width < 10 ? 10 : constraint.width);
		constraint.height = (constraint.height < 10 ? 10 : constraint.height);
		cmd.setLayout(constraint);
		cmd.prepare();
		
		return cmd;
	}
	
	@Override
	protected Command getResizeChildrenCommand(ChangeBoundsRequest request) {
		if (CombinedCommandFactory.isTagged(request))
			return null;
		List<?> editParts = request.getEditParts();
		if (editParts.size() == 0)
			return null;
		Object i_ = editParts.get(0);
		if (!(i_ instanceof GraphicalEditPart))
			return null;
		GraphicalEditPart gep = (GraphicalEditPart)i_;
		Rectangle
			oldConstraint = getCurrentConstraintFor(gep),
			/* sorts out the zoom */
			newConstraint = (Rectangle)getConstraintFor(request, gep);
		request.setMoveDelta(
				newConstraint.getTopLeft().translate(
						oldConstraint.getTopLeft().getNegated()));
		request.setSizeDelta(
				newConstraint.getSize().getShrinked(oldConstraint.getSize()));
		return CombinedCommandFactory.createMoveCommand(request);
	}
}
