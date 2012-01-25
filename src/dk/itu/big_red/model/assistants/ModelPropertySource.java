package dk.itu.big_red.model.assistants;

import java.util.ArrayList;

import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.utilities.Colour;

public class ModelPropertySource implements IPropertySource {
	private ModelObject object;
	
	public ModelPropertySource(ModelObject node) {
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
			Colourable c = (Colourable)object;
			if (c instanceof Link) {
				properties.add(new ColorPropertyDescriptor(Colourable.PROPERTY_OUTLINE, "Colour"));
			} else if (c instanceof Node) {
				properties.add(new ColorPropertyDescriptor(Colourable.PROPERTY_FILL, "Fill colour"));
				properties.add(new ColorPropertyDescriptor(Colourable.PROPERTY_OUTLINE, "Outline colour"));
			}
		}
		
		if (object instanceof ModelObject)
			properties.add(new TextPropertyDescriptor(ModelObject.PROPERTY_COMMENT, "Comment"));
		if (object instanceof Layoutable)
			properties.add(new TextPropertyDescriptor(Layoutable.PROPERTY_NAME, "Name"));
		if (object instanceof Site)
			properties.add(new TextPropertyDescriptor(Site.PROPERTY_ALIAS, "Alias"));
		
		return properties.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals("Class")) {
			return object.getType();
		} else {
			Object value = object.getProperty((String)id);
			if (value instanceof Colour)
				value = ((Colour)value).getRGB();
			if (value == null &&
			    (id.equals(ModelObject.PROPERTY_COMMENT) ||
			     id.equals(Site.PROPERTY_ALIAS)))
				value = "";
			return value;
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
		/* does nothing; never called (see ChangePropertySheetEntry) */
	}
}
