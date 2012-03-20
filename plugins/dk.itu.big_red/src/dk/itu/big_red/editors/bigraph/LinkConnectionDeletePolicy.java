package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

public class LinkConnectionDeletePolicy extends ConnectionEditPolicy {
    @Override
	protected Command getDeleteCommand(GroupRequest request) {
    	return CombinedCommandFactory.createDeleteCommand(request);
    }
}
