package dk.itu.big_red.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.EdgeConnection;

/**
 * EdgeConnectionDeleteCommands remove {@link EdgeConnection}s from their
 * containing {@link Bigraph}.
 * @author alec
 *
 */
public class EdgeConnectionDeleteCommand extends Command {
	private EdgeConnection edgeConnection;

	public void setModel(EdgeConnection edgeConnection) {
		this.edgeConnection = edgeConnection;
	}

	public EdgeConnection getModel() {
		return edgeConnection;
	}
	
	@Override
	public boolean canExecute() {
		return (edgeConnection != null);
	}
	
	@Override
	public void execute() {
		if (canExecute())
			edgeConnection.getParent().removePoint(edgeConnection.getSource());
	}
	
	@Override
	public void undo() {
		edgeConnection.getParent().addPoint(edgeConnection.getSource());
	}
}
