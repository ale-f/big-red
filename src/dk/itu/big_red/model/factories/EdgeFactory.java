package dk.itu.big_red.model.factories;

import org.eclipse.gef.requests.CreationFactory;

import dk.itu.big_red.model.Edge;

public class EdgeFactory implements CreationFactory {
	private Class<? extends Edge> template;
	
	public EdgeFactory(Class<? extends Edge> t) {
		this.template = t;
	}
	
	@Override
	public Object getNewObject() {
		if (template == Edge.class) {
			return new Edge();
		} else {
			return null;
		}
	}
	
	@Override
	public Object getObjectType() {
		return template;
	}

	public static Edge getNewObject(String namedObject) {
		namedObject = namedObject.toLowerCase();
		if (namedObject.equals("edge"))
			return new Edge();
		else return null;
	}
}
