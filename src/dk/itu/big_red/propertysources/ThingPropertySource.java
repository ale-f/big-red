package dk.itu.big_red.propertysources;

import java.util.ArrayList;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import dk.itu.big_red.BigRedConstants;
import dk.itu.big_red.model.IColourable;
import dk.itu.big_red.model.Name;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Thing;

public class ThingPropertySource implements IPropertySource {
	private Thing node;
	
	public ThingPropertySource(Thing node) {
		this.node = node;
	}
	
	@Override
	public Object getEditableValue() {
		// TODO Auto-generated method stub
		return null;
	}

	private String[] controlNames = null;
	
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		ArrayList<IPropertyDescriptor> properties =
			new ArrayList<IPropertyDescriptor>();
		properties.add(new PropertyDescriptor(Thing.PROPERTY_CLASS, "Class"));
		if (node instanceof Node) {
			setControlNames(node.getSignature().getControlNames());
			properties.add(new ComboBoxPropertyDescriptor(Node.PROPERTY_CONTROL, "Control", getControlNames()));
			properties.add(new TextPropertyDescriptor(Node.PROPERTY_COMMENT, "Comment"));
		} else if (node instanceof Name) {
			properties.add(new TextPropertyDescriptor(Name.PROPERTY_NAME, "Name"));
			properties.add(new ComboBoxPropertyDescriptor(Name.PROPERTY_TYPE, "Type", BigRedConstants.INNER_OUTER_NAMES));
		}
		
		if (node instanceof IColourable) {
			properties.add(new ColorPropertyDescriptor(IColourable.PROPERTY_FILL_COLOUR, "Fill colour"));
			properties.add(new ColorPropertyDescriptor(IColourable.PROPERTY_OUTLINE_COLOUR, "Outline colour"));
		}
		return properties.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object id) {
		// TODO Auto-generated method stub
		if (id.equals(Thing.PROPERTY_CLASS)) {
			return node.getClass().getSimpleName();
		} else if (id.equals(Node.PROPERTY_CONTROL)) {
			String targetName = ((Node)node).getControl().getLongName();
			String[] names = getControlNames();
			for (int i = 0; i < names.length; i++) {
				if (names[i].equals(targetName)) {
					return new Integer(i);
				}
			}
			return null;
		} else if (id.equals(IColourable.PROPERTY_FILL_COLOUR)) {
			return ((IColourable)node).getFillColour();
		} else if (id.equals(IColourable.PROPERTY_OUTLINE_COLOUR)) {
			return ((IColourable)node).getOutlineColour();
		} else if (id.equals(Node.PROPERTY_COMMENT)) {
			String result = ((Node)node).getComment();
			return (result == null ? "" : result);
		} else if (id.equals(Name.PROPERTY_NAME)){
			return ((Name)node).getName();
		} else if (id.equals(Name.PROPERTY_TYPE)){
			return ((Name)node).getType().ordinal();
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
		if (id.equals(Node.PROPERTY_CONTROL)) {
			String control = node.getSignature().getControlNames()[(Integer)value];
			((Node)node).setControl(node.getSignature().getControl(control));
		} else if (id.equals(IColourable.PROPERTY_FILL_COLOUR)) {
			((IColourable)node).setFillColour((RGB)value);
		} else if (id.equals(IColourable.PROPERTY_OUTLINE_COLOUR)) {
			((IColourable)node).setOutlineColour((RGB)value);
		} else if (id.equals(Node.PROPERTY_COMMENT)) {
			String comment = (String)value;
			((Node)node).setComment((comment.length() == 0 ? null : comment));
		} else if (id.equals(Name.PROPERTY_NAME)) {
			((Name)node).setName((String)value);
		} else if (id.equals(Name.PROPERTY_TYPE)) {
			((Name)node).setType(Name.NameType.values()[(Integer)value]);
		}
	}

	public void setControlNames(String[] controlNames) {
		this.controlNames = controlNames;
	}

	public String[] getControlNames() {
		return controlNames;
	}

}
