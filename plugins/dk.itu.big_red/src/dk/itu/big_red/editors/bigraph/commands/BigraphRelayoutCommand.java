package dk.itu.big_red.editors.bigraph.commands;

import org.bigraph.model.Bigraph;

import dk.itu.big_red.model.LayoutUtilities;

public class BigraphRelayoutCommand extends ChangeCommand {
	public void setBigraph(Bigraph bigraph) {
		setContext(bigraph);
		setChange(bigraph != null ? LayoutUtilities.relayout(bigraph) : null);
	}
}
