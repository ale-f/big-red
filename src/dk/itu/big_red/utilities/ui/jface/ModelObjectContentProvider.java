package dk.itu.big_red.utilities.ui.jface;

import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import dk.itu.big_red.model.ModelObject;

public abstract class ModelObjectContentProvider
	implements IStructuredContentProvider, PropertyChangeListener {
	private AbstractListViewer alv;
	
	public ModelObjectContentProvider(AbstractListViewer alv) {
		this.alv = alv;
	}
	
	protected AbstractListViewer getViewer() {
		return alv;
	}
	
	private Object input;
	
	protected Object getInput() {
		return input;
	}
	
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
}
