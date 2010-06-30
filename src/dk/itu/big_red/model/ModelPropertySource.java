package dk.itu.big_red.model;

import java.util.ArrayList;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import dk.itu.big_red.BigRedConstants;
import dk.itu.big_red.model.interfaces.IColourable;
import dk.itu.big_red.model.interfaces.ICommentable;

public class ModelPropertySource implements IPropertySource {
	private Object object;
	
	public ModelPropertySource(Object node) {
		this.object = node;
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
		properties.add(new PropertyDescriptor("Class", "Class"));
		if (object instanceof Node) {
			setControlNames(((Node)object).getSignature().getControlNames());
			properties.add(new ComboBoxPropertyDescriptor(Node.PROPERTY_CONTROL, "Control", getControlNames()));
		} else if (object instanceof InnerName) {
			properties.add(new TextPropertyDescriptor(InnerName.PROPERTY_NAME, "Name"));
			properties.add(new ComboBoxPropertyDescriptor(InnerName.PROPERTY_TYPE, "Type", BigRedConstants.INNER_OUTER_NAMES));
		}
		
		if (object instanceof IColourable) {
			properties.add(new ColorPropertyDescriptor(IColourable.PROPERTY_FILL_COLOUR, "Fill colour"));
			properties.add(new ColorPropertyDescriptor(IColourable.PROPERTY_OUTLINE_COLOUR, "Outline colour"));
		}
		if (object instanceof ICommentable) {
			properties.add(new TextPropertyDescriptor(ICommentable.PROPERTY_COMMENT, "Comment"));
		}
		return properties.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals("Class")) {
			return object.getClass().getSimpleName();
		} else if (id.equals(Node.PROPERTY_CONTROL)) {
			String targetName = ((Node)object).getControl().getLongName();
			String[] names = getControlNames();
			for (int i = 0; i < names.length; i++) {
				if (names[i].equals(targetName)) {
					return new Integer(i);
				}
			}
			return null;
		} else if (id.equals(IColourable.PROPERTY_FILL_COLOUR)) {
			return ((IColourable)object).getFillColour();
		} else if (id.equals(IColourable.PROPERTY_OUTLINE_COLOUR)) {
			return ((IColourable)object).getOutlineColour();
		} else if (id.equals(ICommentable.PROPERTY_COMMENT)) {
			String result = ((ICommentable)object).getComment();
			return (result == null ? "" : result);
		} else if (id.equals(InnerName.PROPERTY_NAME)){
			return ((InnerName)object).getName();
		} else if (id.equals(InnerName.PROPERTY_TYPE)){
			return ((InnerName)object).getType().ordinal();
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
			String control = ((Node)object).getSignature().getControlNames()[(Integer)value];
			((Node)object).setControl(((Node)object).getSignature().getControl(control));
		} else if (id.equals(IColourable.PROPERTY_FILL_COLOUR)) {
			((IColourable)object).setFillColour((RGB)value);
		} else if (id.equals(IColourable.PROPERTY_OUTLINE_COLOUR)) {
			((IColourable)object).setOutlineColour((RGB)value);
		} else if (id.equals(ICommentable.PROPERTY_COMMENT)) {
			String comment = (String)value;
			((ICommentable)object).setComment((comment.length() == 0 ? null : comment));
		} else if (id.equals(InnerName.PROPERTY_NAME)) {
			((InnerName)object).setName((String)value);
		} else if (id.equals(InnerName.PROPERTY_TYPE)) {
			((InnerName)object).setType(InnerName.NameType.values()[(Integer)value]);
		}
	}

	public void setControlNames(String[] controlNames) {
		this.controlNames = controlNames;
	}

	public String[] getControlNames() {
		return controlNames;
	}

}
