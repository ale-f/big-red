package dk.itu.big_red.editors.utilities;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

class NullTextPropertyDescriptor extends PropertyDescriptor {
    public NullTextPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    @Override
	public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new NullTextCellEditor(parent);
        if (getValidator() != null)
			editor.setValidator(getValidator());
        return editor;
    }
}
