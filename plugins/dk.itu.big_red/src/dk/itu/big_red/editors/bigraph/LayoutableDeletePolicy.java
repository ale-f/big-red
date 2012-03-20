package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

public class LayoutableDeletePolicy extends ComponentEditPolicy {
	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		return CombinedCommandFactory.createDeleteCommand(deleteRequest);
	}
}
