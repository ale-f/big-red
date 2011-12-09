package dk.itu.big_red.editors.bigraph.commands;

import dk.itu.big_red.model.Bigraph;

public class BigraphRelayoutCommand extends ChangeCommand {
	private Bigraph bigraph = null;
	
	public void setBigraph(Bigraph bigraph) {
		if (bigraph != null)
			this.bigraph = bigraph;
	}
	
	@Override
	public BigraphRelayoutCommand prepare() {
		if (bigraph != null) {
			setTarget(bigraph);
			setChange(bigraph.relayout());
		}
		return this;
	}

}
