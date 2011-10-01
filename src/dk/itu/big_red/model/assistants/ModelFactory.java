package dk.itu.big_red.model.assistants;

import org.eclipse.gef.requests.CreationFactory;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Site;

/**
 * The ModelFactory class creates {@link ModelObject}s on demand.
 * @author alec
 *
 */
public class ModelFactory implements CreationFactory {

	private Class<? extends ModelObject> type;
	
	/**
	 * Creates a new {@link ModelFactory}, ready to produce objects of the
	 * given type.
	 * @param type a {@link Class} (extending {@link ModelObject})
	 */
	public ModelFactory(Class<? extends ModelObject> type) {
		this.type = type;
	}
	
	@Override
	public ModelObject getNewObject() {
		if (type != null) {
			try {
				return type.newInstance();
			} catch (IllegalAccessException e) {
				return null;
			} catch (InstantiationException e) {
				return null;
			}
		} else return null;
	}
	
	@Override
	public Class<? extends ModelObject> getObjectType() {
		return type;
	}

	/**
	 * Creates a new object from a XML tag name.
	 * @param namedObject a XML tag name
	 * @return a new object of the appropriate type, or <code>null</code> if
	 *         the tag name was unrecognised
	 */
	public static ModelObject getNewObject(String namedObject) {
		namedObject = namedObject.toLowerCase();
		if (namedObject.equals("bigraph"))
			return new Bigraph();
		else if (namedObject.equals("root"))
			return new Root();
		else if (namedObject.equals("site"))
			return new Site();
		else if (namedObject.equals("innername"))
			return new InnerName();
		else if (namedObject.equals("outername"))
			return new OuterName();
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
