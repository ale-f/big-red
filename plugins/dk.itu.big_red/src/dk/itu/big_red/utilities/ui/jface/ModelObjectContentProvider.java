package dk.itu.big_red.utilities.ui.jface;

import java.beans.PropertyChangeListener;

import org.bigraph.model.ModelObject;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;

public abstract class ModelObjectContentProvider implements IContentProvider,
		PropertyChangeListener {
	private Object input;
	
	protected Object getInput() {
		return input;
	}
	
	protected void unregister(Object oldInput) {
		if (oldInput instanceof ModelObject)
			((ModelObject)oldInput).removePropertyChangeListener(this);
	}
	
	protected void register(Object newInput) {
		if (newInput instanceof ModelObject)
			((ModelObject)newInput).addPropertyChangeListener(this);
	}
	
	private void setInput(Object oldInput, Object newInput) {
		unregister(oldInput);
		input = newInput;
		register(newInput);
	}
	
	@Override
	public void dispose() {
		setInput(null, null);
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		setInput(oldInput, newInput);
	}
}
