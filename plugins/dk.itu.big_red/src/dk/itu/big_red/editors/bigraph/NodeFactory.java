package dk.itu.big_red.editors.bigraph;

import org.bigraph.model.Control;
import org.bigraph.model.Node;
import org.eclipse.gef.requests.CreationFactory;

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
