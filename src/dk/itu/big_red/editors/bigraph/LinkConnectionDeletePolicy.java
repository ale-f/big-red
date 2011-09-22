package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.editors.bigraph.commands.LinkConnectionDeleteCommand;
import dk.itu.big_red.editors.bigraph.parts.ContainerPart;
import dk.itu.big_red.editors.bigraph.parts.LinkPart;
import dk.itu.big_red.editors.bigraph.parts.PointPart;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.assistants.LinkConnection;

public class LinkConnectionDeletePolicy extends ConnectionEditPolicy {
	private boolean iterativelyCheckContainer(Container c, LinkConnection l) {
		if (c instanceof Node)
			if (l.getPoint().getParent() == c)
				return true;
		
		for (LayoutableModelObject i : c.getChildren())
			if (i instanceof Container)
				if (iterativelyCheckContainer((Container)i, l))
					return true;
		return false;
	}
	
    @Override
	protected Command getDeleteCommand(GroupRequest request) {
    	LinkConnection model = (LinkConnection)getHost().getModel();
    	Link link = model.getLink();
    	Point point = model.getPoint();
    	
    	/*
		 * If this LinkConnection's Link or Point is going to be deleted, then
		 * there's no need to delete this separately.
		 */
    	for (Object i_ : request.getEditParts()) {
    		EditPart i = (EditPart)i_;
    		if (i instanceof LinkPart) {
    			if (((LinkPart)i).getModel() == link)
    				return null;
    		} else if (i instanceof PointPart) {
    			if (((PointPart)i).getModel() == point)
    				return null;
    		}
    		if (i instanceof ContainerPart) {
    			if (iterativelyCheckContainer((Container)i.getModel(), model))
    				return null;
    		}
    	}
    	LinkConnectionDeleteCommand dc = new LinkConnectionDeleteCommand();
		dc.setModel((LinkConnection)getHost().getModel());
		return dc;
    }
}
