package dk.itu.big_red.editpolicies;


import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

import dk.itu.big_red.commands.EdgeCreateCommand;
import dk.itu.big_red.commands.EdgeReconnectSourceCommand;
import dk.itu.big_red.commands.EdgeReconnectTargetCommand;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Edge;

public class ThingEdgePolicy extends GraphicalNodeEditPolicy {
	
	@Override
	protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
		Thing source = (Thing)getHost().getModel();
		EdgeCreateCommand cmd = new EdgeCreateCommand();
		cmd.setSource(source);
		cmd.setObject(new Edge());
		cmd.setInitialClickLocation(request.getLocation());
		request.setStartCommand(cmd);
		return cmd;
	}

	@Override
	protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
		EdgeCreateCommand cmd = 
			(EdgeCreateCommand) request.getStartCommand();
		cmd.setTarget((Thing) getHost().getModel());
		return cmd;
	}
	
	@Override
	protected Command getReconnectTargetCommand(ReconnectRequest request) {
		EdgeReconnectTargetCommand cmd = new EdgeReconnectTargetCommand();
		cmd.setModel((Edge)request.getConnectionEditPart().getModel());
		cmd.setTarget((Thing)getHost().getModel());
		return cmd;
	}
	
	@Override
	protected Command getReconnectSourceCommand(ReconnectRequest request) {
		EdgeReconnectSourceCommand cmd = new EdgeReconnectSourceCommand();
		cmd.setModel((Edge)request.getConnectionEditPart().getModel());
		cmd.setSource((Thing)getHost().getModel());
		return cmd;
	}

}
