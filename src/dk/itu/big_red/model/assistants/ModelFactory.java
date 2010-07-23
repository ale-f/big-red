package dk.itu.big_red.model.assistants;

import org.eclipse.gef.requests.CreationFactory;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Site;

public class ModelFactory implements CreationFactory {

	private Class<?> template;
	
	public ModelFactory(Class<?> t) {
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
		} else if (template == InnerName.class){
			InnerName name = new InnerName();
			return name;
		} else if (template == Edge.class) {
			return new Edge();
		} else {
			return null;
		}
	}
	
	@Override
	public Object getObjectType() {
		return template;
	}

	/**
	 * Creates a new object from a XML tag name.
	 * @param namedObject a XML tag name
	 * @return a new object of the appropriate type, or <code>null</code> if
	 *         the tag name was unrecognised
	 */
	public static Object getNewObject(String namedObject) {
		namedObject = namedObject.toLowerCase();
		if (namedObject.equals("bigraph"))
			return new Bigraph();
		else if (namedObject.equals("root"))
			return new Root();
		else if (namedObject.equals("site"))
			return new Site();
		else if (namedObject.equals("node"))
			return new Node();
		else if (namedObject.equals("innername"))
			return new InnerName();
		else if (namedObject.equals("signature"))
			return new Signature();
		else if (namedObject.equals("port"))
			return new Port();
		else if (namedObject.equals("control"))
			return new Control();
		else if (namedObject.equals("edge"))
			return new Edge();
		else return null;
	}
}
