package dk.itu.big_red.editors.bigraph;

import org.bigraph.model.ModelObject;
import org.eclipse.gef.requests.CreationFactory;

/**
 * The ModelFactory class creates {@link ModelObject}s on demand.
 * @author alec
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
}
