package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.editors.bigraph.commands.LinkConnectionDeleteCommand;
import dk.itu.big_red.model.LinkConnection;

public class LinkConnectionDeletePolicy extends ConnectionEditPolicy {
    @Override
	protected Command getDeleteCommand(GroupRequest request) {
    	LinkConnectionDeleteCommand dc = new LinkConnectionDeleteCommand();
		dc.setModel((LinkConnection)getHost().getModel());
		return dc;
    }
}
