package dk.itu.big_red.editpolicies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.commands.EdgeConnectionDeleteCommand;
import dk.itu.big_red.model.EdgeConnection;

public class EdgeConnectionDeletePolicy extends ConnectionEditPolicy {
    @Override
	protected Command getDeleteCommand(GroupRequest request) {
    	EdgeConnectionDeleteCommand dc = new EdgeConnectionDeleteCommand();
		dc.setModel((EdgeConnection)getHost().getModel());
		return dc;
    }
}
