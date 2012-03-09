package dk.itu.big_red.model.assistants;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ICellEditorValidator;
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
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeRejectedException;

public class ModelPropertySource implements IPropertySource {
	private Layoutable object;
	
	public ModelPropertySource(Layoutable node) {
		object = node;
	}
	
	@Override
	public Object getEditableValue() {
		return null;
	}
	
	private abstract class ChangeValidator implements ICellEditorValidator {
		public abstract Change getChange(Object value);
		
		@Override
		public String isValid(Object value) {
			try {
				object.getBigraph().tryApplyChange(getChange(value));
				return null;
			} catch (ChangeRejectedException cre) {
				return cre.getRationale();
			}
		}
	}
	
	private class NameValidator extends ChangeValidator {
		@Override
		public Change getChange(Object value) {
			return object.changeName((String)value);
		}
	}
	
	private class AliasValidator extends ChangeValidator {
		@Override
		public Change getChange(Object value) {
			return ((Site)object).changeAlias((String)value);
		}
	}
	
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		ArrayList<IPropertyDescriptor> properties =
			new ArrayList<IPropertyDescriptor>();
		properties.add(new PropertyDescriptor("Class", "Class"));
		
		if (object instanceof Link) {
			properties.add(new ColorPropertyDescriptor(Colourable.PROPERTY_OUTLINE, "Colour"));
		} else if (object instanceof Node) {
			properties.add(new ColorPropertyDescriptor(Colourable.PROPERTY_FILL, "Fill colour"));
			properties.add(new ColorPropertyDescriptor(Colourable.PROPERTY_OUTLINE, "Outline colour"));
			if (((Node)object).getControl().getParameterPolicy() != null)
				properties.add(new TextPropertyDescriptor(Node.PROPERTY_PARAMETER, "Parameter"));
		}
		
		if (object instanceof ModelObject)
			properties.add(new TextPropertyDescriptor(ModelObject.PROPERTY_COMMENT, "Comment"));
		if (object instanceof Layoutable) {
			TextPropertyDescriptor d =
					new TextPropertyDescriptor(Layoutable.PROPERTY_NAME, "Name");
			d.setValidator(new NameValidator());
			properties.add(d);
		}
		if (object instanceof Site) {
			TextPropertyDescriptor d =
					new TextPropertyDescriptor(Site.PROPERTY_ALIAS, "Alias");
			d.setValidator(new AliasValidator());
			properties.add(d);
		}
		
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
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
	}

	public static Change getChange(Layoutable object, Object id, Object value) {
		Change c = null;
		if (id.equals(Layoutable.PROPERTY_NAME)) {
			c = object.changeName((String)value);
		} else if (id.equals(ModelObject.PROPERTY_COMMENT)) {
			if ("".equals(value))
				value = null;
			c = object.changeComment((String)value);
		} else if (id.equals(Colourable.PROPERTY_FILL)) {
			c = ((Colourable)object).changeFillColour((Colour)value);
		} else if (id.equals(Colourable.PROPERTY_OUTLINE)) {
			c = ((Colourable)object).changeOutlineColour((Colour)value);
		} else if (id.equals(Site.PROPERTY_ALIAS)) {
			if ("".equals(value))
				value = null;
			c = ((Site)object).changeAlias((String)value);
		}
		return c;
	}
	
	@Override
	public void setPropertyValue(Object id, Object value) {
		Change c = getChange(object, id, value);
		try {
			object.getBigraph().tryApplyChange(c);
		} catch (ChangeRejectedException cre) {
			throw new Error(cre);
		}
	}
}
