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
	
	public boolean compatiblePorts() {
		targetKey = sourceKey = null;
		for (String s : ((Node)source).getControl().getPorts()) {
			for (String t : ((Node)model.getTarget()).getControl().getPorts()) {
				if (source.getPortAuthority().canConnect(s, t)) {
					sourceKey = s;
					targetKey = t;
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean canExecute() {
		return (model != null && source != null && model.sourceOK(source) && compatiblePorts());
	}
	
	public void execute() {
		if (canExecute()) {
			model.getTarget().removeEdge(model);
			oldSource.removeEdge(model);
			((Node)source).connect(sourceKey, (Node)model.getTarget(), targetKey, model);
		}
	}
	
	public void undo() {
		model.getTarget().removeEdge(model);
		source.removeEdge(model);
		((Node)oldSource).connect(oldSourceKey, (Node)model.getTarget(), oldTargetKey, model);
	}
}
