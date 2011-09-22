package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import dk.itu.big_red.editors.bigraph.commands.LayoutableDeleteCommand;


public class LayoutableDeletePolicy extends ComponentEditPolicy {
	@Override
	protected Command createDeleteCommand(GroupRequest deleteRequest) {
		LayoutableDeleteCommand command = new LayoutableDeleteCommand();
		command.setObject(getHost().getModel());
		command.prepare();
		return command;
	}
}
