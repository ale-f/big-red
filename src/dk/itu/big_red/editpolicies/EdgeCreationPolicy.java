package dk.itu.big_red.editpolicies;


import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import dk.itu.big_red.commands.EdgeCreateCommand;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.interfaces.IConnectable;

public class EdgeCreationPolicy extends GraphicalNodeEditPolicy {
	
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		IConnectable source = (IConnectable)getHost().getModel();
		EdgeCreateCommand cmd = new EdgeCreateCommand();
		cmd.setSource(source);
		cmd.setObject(new Edge());
		request.setStartCommand(cmd);
		return cmd;
	}

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		EdgeCreateCommand cmd = 
			(EdgeCreateCommand) request.getStartCommand();
		cmd.setTarget((IConnectable) getHost().getModel());
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
