package dk.itu.big_red.commands;



import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.*;



public class EdgeDeleteCommand extends Command {
	private Edge edge;
	private Thing source, target;
	
	public EdgeDeleteCommand() {
		
	}
	
	public EdgeDeleteCommand(Edge e) {
		setModel(e);
	}
	
	public void setModel(Object model) {
		if (model instanceof Edge) {
			this.edge = (Edge)model;
			this.source = this.edge.getSource();
			this.target = this.edge.getTarget();
		}
	}
	
	public void execute() {
		source.removeEdge(edge);
		target.removeEdge(edge);
	}
	
	public void undo() {
		source.addEdge(edge);
		target.addEdge(edge);
	}
}
