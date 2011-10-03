package dk.itu.big_red.model.assistants;

import java.util.ArrayList;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeName;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.IFillColourable;
import dk.itu.big_red.model.interfaces.internal.IOutlineColourable;

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
		
		if (object instanceof IFillColourable)
			properties.add(new ColorPropertyDescriptor(IFillColourable.PROPERTY_FILL_COLOUR, "Fill colour"));
		if (object instanceof IOutlineColourable)
			properties.add(new ColorPropertyDescriptor(IOutlineColourable.PROPERTY_OUTLINE_COLOUR, "Outline colour"));
		if (object instanceof ICommentable)
			properties.add(new TextPropertyDescriptor(ICommentable.PROPERTY_COMMENT, "Comment"));
		if (object instanceof Layoutable)
			properties.add(new TextPropertyDescriptor(Layoutable.PROPERTY_NAME, "Name"));
		
		return properties.toArray(new IPropertyDescriptor[0]);
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (id.equals("Class")) {
			return object.getClass().getSimpleName();
		} else if (id.equals(IFillColourable.PROPERTY_FILL_COLOUR)) {
			return ((IFillColourable)object).getFillColour();
		} else if (id.equals(IOutlineColourable.PROPERTY_OUTLINE_COLOUR)) {
			return ((IOutlineColourable)object).getOutlineColour();
		} else if (id.equals(ICommentable.PROPERTY_COMMENT)) {
			String result = ((ICommentable)object).getComment();
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
		if (id.equals(IFillColourable.PROPERTY_FILL_COLOUR)) {
			((IFillColourable)object).setFillColour((RGB)value);
		} else if (id.equals(IOutlineColourable.PROPERTY_OUTLINE_COLOUR)) {
			((IOutlineColourable)object).setOutlineColour((RGB)value);
		} else if (id.equals(ICommentable.PROPERTY_COMMENT)) {
			String comment = (String)value;
			((ICommentable)object).setComment((comment.length() == 0 ? null : comment));
		} else if (id.equals(Layoutable.PROPERTY_NAME)) {
			String name = (String)value;
			Layoutable l = (Layoutable)object;
			l.getBigraph().applyChange(new BigraphChangeName(l, name));
		}
	}

}
