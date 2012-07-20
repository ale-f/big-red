package dk.itu.big_red.utilities.ui;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import dk.itu.big_red.utilities.ui.jface.NullTextCellEditor;

public class NullTextPropertyDescriptor extends PropertyDescriptor {
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
