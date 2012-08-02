package dk.itu.big_red.editors.utilities;

import java.util.ArrayList;

import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.Site;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.ParameterUtilities;
import dk.itu.big_red.utilities.ui.NullTextPropertyDescriptor;

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
		public abstract IChange getChange(Object value);
		
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
		public IChange getChange(Object value) {
			return object.changeName((String)value);
		}
	}
	
	private class AliasValidator extends ChangeValidator {
		@Override
		public IChange getChange(Object value) {
			return ExtendedDataUtilities.changeAlias(
					((Site)object), (String)value);
		}
	}
	
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		ArrayList<IPropertyDescriptor> properties =
			new ArrayList<IPropertyDescriptor>();
		properties.add(new PropertyDescriptor("Class", "Class"));
		
		if (object instanceof Link) {
			properties.add(new ColorPropertyDescriptor(ColourUtilities.OUTLINE, "Colour"));
		} else if (object instanceof Node) {
			properties.add(new ColorPropertyDescriptor(ColourUtilities.FILL, "Fill colour"));
			properties.add(new ColorPropertyDescriptor(ColourUtilities.OUTLINE, "Outline colour"));
			if (ParameterUtilities.getParameterPolicy(((Node)object).getControl()) != null)
				properties.add(new NullTextPropertyDescriptor(ParameterUtilities.PARAMETER, "Parameter"));
		}
		
		if (object instanceof ModelObject)
			properties.add(new NullTextPropertyDescriptor(ExtendedDataUtilities.COMMENT, "Comment"));
		if (object instanceof Layoutable) {
			NullTextPropertyDescriptor d =
					new NullTextPropertyDescriptor(Layoutable.PROPERTY_NAME, "Name");
			d.setValidator(new NameValidator());
			properties.add(d);
		}
		if (object instanceof Site) {
			NullTextPropertyDescriptor d =
					new NullTextPropertyDescriptor(ExtendedDataUtilities.ALIAS, "Alias");
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
			if (ParameterUtilities.PARAMETER.equals(id)) {
				return ParameterUtilities.getParameter((Node)object);
			} else if (ExtendedDataUtilities.COMMENT.equals(id)) {
				return ExtendedDataUtilities.getComment(object);
			} else if (ColourUtilities.FILL.equals(id)) {
				return ColourUtilities.getFill(object).getRGB();
			} else if (ColourUtilities.OUTLINE.equals(id)) {
				return ColourUtilities.getOutline(object).getRGB();
			} else if (ExtendedDataUtilities.ALIAS.equals(id)) {
				return ExtendedDataUtilities.getAlias(((Site)object));
			} else if (Layoutable.PROPERTY_NAME.equals(id)) {
				return object.getName();
			} else return null;
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
