package dk.itu.big_red.commands;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.LinkConnection;

/**
 * EdgeConnectionDeleteCommands remove {@link LinkConnection}s from their
 * containing {@link Bigraph}.
 * @author alec
 *
 */
public class LinkConnectionDeleteCommand extends Command {
	private LinkConnection linkConnection;

	public void setModel(LinkConnection linkConnection) {
		this.linkConnection = linkConnection;
	}

	public LinkConnection getModel() {
		return linkConnection;
	}
	
	@Override
	public boolean canExecute() {
		return (linkConnection != null);
	}
	
	@Override
	public void execute() {
		if (canExecute())
			linkConnection.getLink().removePoint(linkConnection.getPoint());
	}
	
	@Override
	public void undo() {
		linkConnection.getLink().addPoint(linkConnection.getPoint());
	}
}
