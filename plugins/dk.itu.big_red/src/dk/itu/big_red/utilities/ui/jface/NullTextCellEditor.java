package dk.itu.big_red.utilities.ui.jface;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Composite;

public class NullTextCellEditor extends TextCellEditor {
	public NullTextCellEditor() {
		super();
	}

	public NullTextCellEditor(Composite parent) {
		super(parent);
	}
	
	public NullTextCellEditor(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected String doGetValue() {
		String s = text.getText().trim();
		return (s.length() > 0 ? s : null);
	}
	
	@Override
	protected void doSetValue(Object value) {
		super.doSetValue(value != null ? value : "");
	}
	
	@Override
	protected void editOccured(ModifyEvent e) {
        String value = doGetValue();
        boolean oldValidState = isValueValid();
        boolean newValidState = isCorrect(value);
        if (!newValidState)
            setErrorMessage(MessageFormat.format(getErrorMessage(),
                    new Object[] { value }));
        valueChanged(oldValidState, newValidState);
	}
}
