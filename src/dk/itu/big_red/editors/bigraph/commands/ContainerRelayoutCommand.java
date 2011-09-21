package dk.itu.big_red.editors.bigraph.commands;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.changes.ChangeGroup;

public class ContainerRelayoutCommand extends ChangeCommand {
	private ChangeGroup cg = new ChangeGroup();
	
	public ContainerRelayoutCommand() {
		setChange(cg);
	}
	
	protected Container model = null;
	
	public void setModel(Object model) {
		if (model instanceof Container && !(model instanceof Bigraph)) {
			this.model = (Container)model;
			super.setTarget(this.model.getBigraph());
			cg.clear();
			this.model.relayout(cg);
		}
	}

	public Container getModel() {
		return model;
	}
}
