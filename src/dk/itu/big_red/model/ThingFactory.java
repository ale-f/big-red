package dk.itu.big_red.model;

import org.eclipse.gef.requests.CreationFactory;

public class ThingFactory implements CreationFactory {

	private Class<?> template;
	
	public ThingFactory(Class<?> t) {
		this.template = t;
	}
	
	@Override
	public Object getNewObject() {
		if (template == null) {
			return null;
		} else if (template == Node.class) {
			Node node = new Node();
			return node;
		} else if (template == Root.class) {
			Root root = new Root();
			return root;
		} else if (template == Site.class) {
			Site site = new Site();
			return site;
		} else if (template == Edge.class) {
			/*
			 * So Edges aren't *technically* Things. Oh, well; who cares?
			 */
			Edge edge = new Edge();
			return edge;
		} else if (template == Name.class){
			Name name = new Name();
			return name;
		} else {
			return null;
		}
	}
	
	@Override
	public Object getObjectType() {
		return template;
	}

	public static Thing getNewObject(String namedObject) {
		namedObject = namedObject.toLowerCase();
		if (namedObject.equals("root"))
			return new Root();
		else if (namedObject.equals("site"))
			return new Site();
		else if (namedObject.equals("node"))
			return new Node();
		else if (namedObject.equals("name"))
			return new Name();
		else return null;
	}
}
