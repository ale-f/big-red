package dk.itu.big_red.model.factories;

import org.eclipse.gef.requests.CreationFactory;

import dk.itu.big_red.model.Name;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.Thing;

public class ThingFactory implements CreationFactory {

	private Class<? extends Thing> template;
	
	public ThingFactory(Class<? extends Thing> t) {
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
