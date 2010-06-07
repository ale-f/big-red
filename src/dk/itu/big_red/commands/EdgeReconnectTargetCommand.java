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
	
	public boolean compatiblePorts() {
		sourceKey = targetKey = null;
		for (String s : ((Node)target).getControl().getPorts()) {
			for (String t : ((Node)model.getSource()).getControl().getPorts()) {
				if (target.getSignature().canConnect(s, t)) {
					targetKey = s;
					sourceKey = t;
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean canExecute() {
		return (model != null && target != null && model.targetOK(target) && compatiblePorts());
	}
	
	public void execute() {
		if (canExecute()) {
			model.getSource().removeEdge(model);
			oldTarget.removeEdge(model);
			((Node)model.getSource()).connect(sourceKey, (Node)target, targetKey, model);
		}
	}
	
	public void undo() {
		model.getSource().removeEdge(model);
		target.removeEdge(model);
		((Node)model.getSource()).connect(oldSourceKey, (Node)oldTarget, oldTargetKey, model);
	}
}