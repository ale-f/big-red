package dk.itu.big_red.editors.signature;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import org.bigraph.model.Control;
import org.bigraph.model.Signature;
import org.eclipse.jface.viewers.AbstractTreeViewer;

import dk.itu.big_red.utilities.ui.jface.ModelObjectTreeContentProvider;

class SignatureControlsContentProvider extends ModelObjectTreeContentProvider {
	public SignatureControlsContentProvider(AbstractTreeViewer atv) {
		super(atv);
	}
	
	private void recursivelyListen(Signature s) {
		s.addPropertyChangeListener(this);
		for (Control c : s.getControls())
			c.addPropertyChangeListener(this);
		for (Signature t : s.getSignatures())
			recursivelyListen(t);
	}
	
	private void recursivelyStopListening(Signature s) {
		for (Signature t : s.getSignatures())
			recursivelyStopListening(t);
		for (Control c : s.getControls())
			c.removePropertyChangeListener(this);
		s.removePropertyChangeListener(this);
	}
	
	@Override
	protected void unregister(Object oldInput) {
		if (oldInput instanceof Signature)
			recursivelyStopListening((Signature)oldInput);
	}
	
	@Override
	protected void register(Object newInput) {
		if (newInput instanceof Signature)
			recursivelyListen((Signature)newInput);
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Signature) {
			Signature s = (Signature)parentElement;
			ArrayList<Object> r = new ArrayList<Object>();
			r.addAll(s.getSignatures());
			r.addAll(s.getControls());
			return r.toArray();
		} else return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof Signature) {
			return ((Signature)element).getParent();
		} else if (element instanceof Control) {
			return ((Control)element).getSignature();
		} else return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Signature) {
			Signature s = (Signature)element;
			return (s.getControls().size() > 0 ||
					s.getSignatures().size() > 0);
		} else return false;
	}
	
	private void updateViewer(Object object, String... properties) {
		getViewer().update(object, properties);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object
			source = evt.getSource(),
			oldValue = evt.getOldValue(),
			newValue = evt.getNewValue();
		String pn = evt.getPropertyName();
		if (source instanceof Signature) {
			if (Signature.PROPERTY_CONTROL.equals(pn)) {
				if (oldValue == null) {
					Control c = (Control)newValue;
					getViewer().add(c.getSignature(), c);
					c.addPropertyChangeListener(this);
				} else if (newValue == null) {
					Control c = (Control)oldValue;
					c.removePropertyChangeListener(this);
					getViewer().remove(c);
				}
			} else if (Signature.PROPERTY_CHILD.equals(pn)) {
				if (oldValue == null) {
					Signature s = (Signature)newValue;
					getViewer().add(s.getParent(), s);
					s.addPropertyChangeListener(this);
				} else if (newValue == null) {
					Signature s = (Signature)oldValue;
					s.removePropertyChangeListener(this);
					getViewer().remove(s);
				}
			}
		}
		updateViewer(source, pn);
	}
}
