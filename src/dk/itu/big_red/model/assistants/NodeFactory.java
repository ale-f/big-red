package dk.itu.big_red.model.assistants;

import org.eclipse.gef.requests.CreationFactory;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Node;

public class NodeFactory implements CreationFactory {
	private Control template;
	
	public NodeFactory(Control template) {
		this.template = template;
	}
	
	@Override
	public Object getNewObject() {
		return new Node(template);
	}

	@Override
	public Object getObjectType() {
		return template;
	}

}
