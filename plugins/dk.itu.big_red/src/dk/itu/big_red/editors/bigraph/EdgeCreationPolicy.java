package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import dk.itu.big_red.editors.bigraph.commands.LinkConnectionCreateCommand;

public class EdgeCreationPolicy extends GraphicalNodeEditPolicy {
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		LinkConnectionCreateCommand cmd = new LinkConnectionCreateCommand();
		cmd.setFirst(getHost().getModel());
		request.setStartCommand(cmd);
		return cmd;
	}

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		LinkConnectionCreateCommand cmd = 
			(LinkConnectionCreateCommand) request.getStartCommand();
		cmd.setSecond(getHost().getModel());
		cmd.prepare();
		return cmd;
	}
	
	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		return null;
	}
	
	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		return null;
	}
}
