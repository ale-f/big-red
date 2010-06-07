package dk.itu.big_red.commands;


import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.*;

public class EdgeReconnectTargetCommand extends Command {
	private Edge model;
	private Thing target, oldTarget;
	private String sourceKey, targetKey, oldTargetKey, oldSourceKey;
	
	public void setModel(Edge model) {
		this.model = model;
		oldTarget = this.model.getTarget();
		oldTargetKey = model.getTargetKey();
		oldSourceKey = model.getSourceKey();
	}
	
	public void setTarget(Thing node) {
		this.target = node;
	}
	
	public boolean canExecute() {
		return false;
	}
	
	public void execute() {
	}
	
	public void undo() {
	}
}