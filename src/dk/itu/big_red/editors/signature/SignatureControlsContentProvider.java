package dk.itu.big_red.editors.signature;

import java.beans.PropertyChangeEvent;
import org.eclipse.jface.viewers.AbstractListViewer;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.utilities.ui.jface.ModelObjectContentProvider;

public class SignatureControlsContentProvider
	extends ModelObjectContentProvider {
	public SignatureControlsContentProvider(AbstractListViewer alv) {
		super(alv);
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return ((Signature)inputElement).getControls().toArray();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!evt.getSource().equals(getInput()))
			return;
		String propertyName = evt.getPropertyName();
		Object oldValue = evt.getOldValue(), newValue = evt.getNewValue();
		if (propertyName.equals(Signature.PROPERTY_CONTROL)) {
			if (oldValue == null && newValue != null) { /* added */
				getViewer().add(newValue);
			} else if (oldValue != null && newValue == null) { /* removed */
				getViewer().remove(oldValue);
			}
		}
	}
}
