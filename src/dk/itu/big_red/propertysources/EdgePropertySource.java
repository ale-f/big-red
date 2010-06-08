package dk.itu.big_red.propertysources;

import java.util.ArrayList;



import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Edge;

public class EdgePropertySource implements IPropertySource {
	private Edge edge;
	
	public EdgePropertySource(Edge edge) {
		this.edge = edge;
	}

	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		ArrayList<IPropertyDescriptor> properties =
			new ArrayList<IPropertyDescriptor>();
		properties.add(new PropertyDescriptor("Class", "Class"));
		properties.add(new TextPropertyDescriptor(Edge.PROPERTY_COMMENT, "Comment"));
		return properties.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals("Class")) {
			return edge.getClass().getSimpleName();
		} else if (id.equals(Edge.PROPERTY_COMMENT)) {
			return edge.getComment();
		} else {
			return null;
		}
	}

	@Override
	public boolean isPropertySet(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		if (id.equals(Edge.PROPERTY_COMMENT)) {
			edge.setComment((String)value);
		}
	}

}
