package dk.itu.big_red.editors.bigraph.commands;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.assistants.LinkConnection;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeDisconnect;

/**
 * EdgeConnectionDeleteCommands remove {@link LinkConnection}s from their
 * containing {@link Bigraph}.
 * @author alec
 *
 */
public class LinkConnectionDeleteCommand extends ChangeCommand {
	private LinkConnection lc = null;
	
	public void setModel(LinkConnection l) {
		if (l != null)
			lc = l;
		prepare();
	}

	@Override
	public void prepare() {
		if (lc == null)
			return;
		setChange(new BigraphChangeDisconnect(lc.getPoint(), lc.getLink()));
	}
}
