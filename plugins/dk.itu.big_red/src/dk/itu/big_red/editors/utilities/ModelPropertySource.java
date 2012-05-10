package dk.itu.big_red.editors.utilities;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Site;
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
				object.getBigraph().tryValidateChange(getChange(value));
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
			properties.add(new ColorPropertyDescriptor(ExtendedDataUtilities.OUTLINE, "Colour"));
		} else if (object instanceof Node) {
			properties.add(new ColorPropertyDescriptor(ExtendedDataUtilities.FILL, "Fill colour"));
			properties.add(new ColorPropertyDescriptor(ExtendedDataUtilities.OUTLINE, "Outline colour"));
			if (ExtendedDataUtilities.getParameterPolicy(((Node)object).getControl()) != null)
				properties.add(new TextPropertyDescriptor(ExtendedDataUtilities.PARAMETER, "Parameter"));
		}
		
		if (object instanceof ModelObject)
			properties.add(new TextPropertyDescriptor(ExtendedDataUtilities.COMMENT, "Comment"));
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
			if (ExtendedDataUtilities.PARAMETER.equals(id)) {
				return ExtendedDataUtilities.getParameter((Node)object);
			} else if (ExtendedDataUtilities.COMMENT.equals(id)) {
				return ExtendedDataUtilities.getComment(object);
			} else if (ExtendedDataUtilities.FILL.equals(id)) {
				return ExtendedDataUtilities.getFill(object).getRGB();
			} else if (ExtendedDataUtilities.OUTLINE.equals(id)) {
				return ExtendedDataUtilities.getOutline(object).getRGB();
			} else {
				Object value = object.getProperty((String)id);
				if (value == null &&
				     Site.PROPERTY_ALIAS.equals(id))
					value = "";
				return value;
			}
		}
	}

	@Override
	public boolean isPropertySet(Object id) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		/* does nothing; never called (see ChangePropertySheetEntry) */
	}
}
