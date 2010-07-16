package dk.itu.big_red.editpolicies;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.commands.ILayoutableAddCommand;
import dk.itu.big_red.commands.ILayoutableCreateCommand;
import dk.itu.big_red.commands.ILayoutableOrphanCommand;
import dk.itu.big_red.commands.ILayoutableRelayoutCommand;
import dk.itu.big_red.figure.*;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.part.*;

public class ILayoutableLayoutPolicy extends XYLayoutEditPolicy {

	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		ILayoutableRelayoutCommand command = null;
		if (!(child instanceof BigraphPart)) {
			command = new ILayoutableRelayoutCommand();
			
			command.setModel(child.getModel());
			command.setConstraint(constraint);
		}
		return command;
	}
	
	@Override
	protected Command createAddCommand(EditPart child, Object constraint) {
		ILayoutableAddCommand command = new ILayoutableAddCommand();
		command.setParent(getHost().getModel());
		command.setChild(child.getModel());
		command.setConstraint(constraint);
		return command;
	}
	
	@Override
	protected Command getOrphanChildrenCommand(Request request) {
		ILayoutableOrphanCommand command = null;
		if (request.getType() == REQ_ORPHAN_CHILDREN && request instanceof GroupRequest) {
			GroupRequest groupRequest = (GroupRequest)request;
			command = new ILayoutableOrphanCommand();
			
			Object model =
				((EditPart)groupRequest.getEditParts().get(0)).getModel();
			if (model instanceof ILayoutable)
				command.setParent(((ILayoutable)model).getParent());
			
			command.setChildren(groupRequest.getEditParts());
		}
		return command;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Object requestObject = request.getNewObject();
		
		Class<? extends ILayoutable> type =
			(Class<? extends ILayoutable>)requestObject.getClass();
		int defWidth = 0, defHeight = 0;
		ILayoutable parent = (ILayoutable)getHost().getModel();
		if (!parent.canContain((ILayoutable)requestObject)) {
			return null;
		} else if (type == Node.class) {
			defWidth = NodeFigure.NODE_FIGURE_DEFWIDTH;
			defHeight = NodeFigure.NODE_FIGURE_DEFHEIGHT;
		} else if (type == Root.class) {
			defWidth = RootFigure.ROOT_FIGURE_DEFWIDTH;
			defHeight = RootFigure.ROOT_FIGURE_DEFHEIGHT;
		} else if (type == Site.class) {
			defWidth = SiteFigure.SITE_FIGURE_DEFWIDTH;
			defHeight = SiteFigure.SITE_FIGURE_DEFHEIGHT;
		} else if (type == InnerName.class){
			defWidth = InnerNameFigure.SITE_FIGURE_DEFWIDTH;
			defHeight = InnerNameFigure.SITE_FIGURE_DEFHEIGHT;
		}
		
		ILayoutableCreateCommand cmd = new ILayoutableCreateCommand();
		cmd.setContainer(getHost().getModel());
		cmd.setObject(request.getNewObject());
		
		Rectangle constraint = (Rectangle)getConstraintFor(request);
		constraint.x = (constraint.x < 0 ? 0 : constraint.x);
		constraint.y = (constraint.y < 0 ? 0 : constraint.y);
		constraint.width = (constraint.width <= 0 ? defWidth : constraint.width);
		constraint.height = (constraint.height <= 0 ? defHeight : constraint.height);
		cmd.setLayout(constraint);
		
		return cmd;
	}

}
