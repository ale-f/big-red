package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.requests.CreationFactory;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Node;

public class NodeFactory implements CreationFactory {
	private Control template;
	
	public NodeFactory(Control template) {
		this.template = template;
	}
	
	@Override
	public Node getNewObject() {
		return new Node(template);
	}

	@Override
	public Control getObjectType() {
		return template;
	}
}
