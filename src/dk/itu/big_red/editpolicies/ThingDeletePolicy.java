package dk.itu.big_red.editpolicies;



import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.commands.ThingDeleteCommand;

public class ThingDeletePolicy extends ComponentEditPolicy {
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		ThingDeleteCommand command = new ThingDeleteCommand();
		command.setModel(getHost().getModel());
		command.setParentModel(getHost().getParent().getModel());
		return command;
	}
}
