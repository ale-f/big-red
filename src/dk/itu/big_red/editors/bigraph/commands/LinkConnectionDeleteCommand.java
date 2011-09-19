package dk.itu.big_red.editors.bigraph.commands;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.assistants.LinkConnection;
import dk.itu.big_red.model.changes.BigraphChangeDisconnect;

/**
 * EdgeConnectionDeleteCommands remove {@link LinkConnection}s from their
 * containing {@link Bigraph}.
 * @author alec
 *
 */
public class LinkConnectionDeleteCommand extends ChangeCommand {
	public void setModel(LinkConnection l) {
		setTarget(l.getLink().getBigraph());
		setChange(new BigraphChangeDisconnect(l.getPoint(), l.getLink()));
	}
}
