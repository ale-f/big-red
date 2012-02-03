package dk.itu.big_red.editors.signature;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Signature;

public class SignatureControlsContentProvider implements
		IStructuredContentProvider, PropertyChangeListener {

	private AbstractListViewer alv;
	
	public SignatureControlsContentProvider(AbstractListViewer alv) {
		this.alv = alv;
	}
	
	private Object input;
	
	@Override
	public void dispose() {
		if (input instanceof ModelObject)
			((ModelObject)input).removePropertyChangeListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (alv != viewer)
			System.err.println(this + ".inputChanged: viewer != alv");
		if (oldInput instanceof ModelObject)
			((ModelObject)oldInput).removePropertyChangeListener(this);
		input = newInput;
		if (newInput instanceof ModelObject)
			((ModelObject)newInput).addPropertyChangeListener(this);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return ((Signature)inputElement).getControls().toArray();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (!evt.getSource().equals(input))
			return;
		String propertyName = evt.getPropertyName();
		Object oldValue = evt.getOldValue(), newValue = evt.getNewValue();
		if (propertyName.equals(Signature.PROPERTY_CONTROL)) {
			if (oldValue == null && newValue != null) { /* added */
				alv.add(newValue);
			} else if (oldValue != null && newValue == null) { /* removed */
				alv.remove(oldValue);
			}
		}
	}
}
