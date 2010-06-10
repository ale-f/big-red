package dk.itu.big_red.editpolicies;



import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.model.Edge;



public class EdgeEditPolicy extends ConnectionEditPolicy {
	private Edge getCastedModel() {
		return (Edge)getHost().getModel();
	}
	
    protected Command getDeleteCommand(GroupRequest request) {
        return null;
    }
}
