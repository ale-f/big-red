package dk.itu.big_red.model.assistants;

import java.util.ArrayList;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.util.Colour;

public class ModelPropertySource implements IPropertySource {
	private Object object;
	
	public ModelPropertySource(Object node) {
		object = node;
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
		
		if (object instanceof Colourable) {
			properties.add(new ColorPropertyDescriptor(Colourable.PROPERTY_FILL, "Fill colour"));
			properties.add(new ColorPropertyDescriptor(Colourable.PROPERTY_OUTLINE, "Outline colour"));
		}
		
		if (object instanceof ModelObject)
			properties.add(new TextPropertyDescriptor(ModelObject.PROPERTY_COMMENT, "Comment"));
		if (object instanceof Layoutable)
			properties.add(new TextPropertyDescriptor(Layoutable.PROPERTY_NAME, "Name"));
		
		return properties.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals("Class")) {
			return object.getClass().getSimpleName();
		} else if (id.equals(Colourable.PROPERTY_FILL)) {
			return ((Colourable)object).getFillColour().getRGB();
		} else if (id.equals(Colourable.PROPERTY_OUTLINE)) {
			return ((Colourable)object).getOutlineColour().getRGB();
		} else if (id.equals(ModelObject.PROPERTY_COMMENT)) {
			String result = ((ModelObject)object).getComment();
			return (result == null ? "" : result);
		} else if (id.equals(Layoutable.PROPERTY_NAME)){
			return ((Layoutable)object).getName();
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
		/*
		 * XXX: as currently implemented, there's no way for Nodes to get
		 * their original shape back (because "Undo Set Control property"
		 * just calls this function with the previous Control value).
		 */
		if (id.equals(Colourable.PROPERTY_FILL)) {
			((Colourable)object).setFillColour(new Colour((RGB)value));
		} else if (id.equals(Colourable.PROPERTY_OUTLINE)) {
			((Colourable)object).setOutlineColour(new Colour((RGB)value));
		} else if (id.equals(ModelObject.PROPERTY_COMMENT)) {
			String comment = (String)value;
			((ModelObject)object).setComment((comment.length() == 0 ? null : comment));
		} else if (id.equals(Layoutable.PROPERTY_NAME)) {
			String name = (String)value;
			Layoutable l = (Layoutable)object;
			l.getBigraph().applyChange(l.changeName(name));
		}
	}

}
