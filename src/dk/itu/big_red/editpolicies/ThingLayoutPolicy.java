package dk.itu.big_red.editpolicies;



import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import dk.itu.big_red.commands.ThingCreateCommand;
import dk.itu.big_red.commands.ILayoutableRelayoutCommand;
import dk.itu.big_red.figure.*;
import dk.itu.big_red.model.*;
import dk.itu.big_red.part.*;

public class ThingLayoutPolicy extends XYLayoutEditPolicy {

	@Override
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		ILayoutableRelayoutCommand command = null;
		if (!(child instanceof BigraphPart)) {
			command = new ILayoutableRelayoutCommand();
			
			command.setModel(child.getModel());
			command.setConstraint((Rectangle)constraint);
		}
		return command;
	}
	
	@Override
	protected Command getOrphanChildrenCommand(Request request) {
		if (request.getType() == REQ_ORPHAN_CHILDREN) {
			return null; // new OrphanCommand(request.getExtendedData());
		} else return super.getOrphanChildrenCommand(request);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Object requestObject = request.getNewObject();
		
		Class type = (Class)(requestObject.getClass());
		int defWidth = 0, defHeight = 0;
		Thing parent = (Thing)getHost().getModel();
		if (!parent.canContain((Thing)requestObject)) {
			return null;
		}
		if (type == Node.class) {
			defWidth = NodeFigure.NODE_FIGURE_DEFWIDTH;
			defHeight = NodeFigure.NODE_FIGURE_DEFHEIGHT;
		} else if (type == Root.class) {
			defWidth = RootFigure.ROOT_FIGURE_DEFWIDTH;
			defHeight = RootFigure.ROOT_FIGURE_DEFHEIGHT;
		} else if (type == Site.class) {
			defWidth = SiteFigure.SITE_FIGURE_DEFWIDTH;
			defHeight = SiteFigure.SITE_FIGURE_DEFHEIGHT;
		} else if (type == Name.class){
			defWidth = NameFigure.SITE_FIGURE_DEFWIDTH;
			defHeight = NameFigure.SITE_FIGURE_DEFHEIGHT;
		}
		
		ThingCreateCommand cmd = new ThingCreateCommand();
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
