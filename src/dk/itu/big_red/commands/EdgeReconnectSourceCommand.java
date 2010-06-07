package dk.itu.big_red.commands;


import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.*;

public class EdgeReconnectSourceCommand extends Command {
	private Edge model;
	private Thing source, oldSource;
	private String targetKey, sourceKey, oldSourceKey, oldTargetKey;
	
	public void setModel(Edge model) {
		this.model = model;
		oldSource = this.model.getSource();
		oldSourceKey = model.getSourceKey();
		oldTargetKey = model.getTargetKey();
	}
	
	public void setSource(Thing node) {
		this.source = node;
	}
	
	public boolean canExecute() {
		return false;
	}
	
	public void execute() {
	}
	
	public void undo() {
	}
}
