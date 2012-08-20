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
	
	protected void setInput(Object oldInput, Object newInput) {
		if (oldInput instanceof ModelObject)
			((ModelObject)oldInput).removePropertyChangeListener(this);
		input = newInput;
		if (input instanceof ModelObject)
			((ModelObject)input).addPropertyChangeListener(this);
	}
	
	protected void setInput(Object newInput) {
		setInput(input, newInput);
	}
	
	@Override
	public void dispose() {
		setInput(null);
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		setInput(oldInput, newInput);
	}
}
