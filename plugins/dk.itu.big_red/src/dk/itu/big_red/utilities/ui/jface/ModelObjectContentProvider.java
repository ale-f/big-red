package dk.itu.big_red.utilities.ui.jface;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.ModelObject;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;

public abstract class ModelObjectContentProvider implements IContentProvider,
		PropertyChangeListener {
	private List<ModelObject> listeningTo = new ArrayList<ModelObject>();
	
	protected void listenTo(ModelObject m) {
		m.addPropertyChangeListener(this);
		listeningTo.add(m);
	}
	
	protected void stopListeningTo(ModelObject m) {
		listeningTo.remove(m);
		m.removePropertyChangeListener(this);
	}
	
	private Object input;
	
	protected Object getInput() {
		return input;
	}
	
	/**
	 * Subclasses should override to update their property change listeners.
	 * @param oldInput
	 * @param newInput
	 */
	protected void setInput(Object oldInput, Object newInput) {
		if (oldInput instanceof ModelObject)
			stopListeningTo((ModelObject)oldInput);
		input = newInput;
		if (input instanceof ModelObject)
			listenTo((ModelObject)input);
	}
	
	protected final void setInput(Object newInput) {
		setInput(input, newInput);
	}
	
	@Override
	public void dispose() {
		setInput(null);
		
		for (ModelObject m : listeningTo)
			stopListeningTo(m);
		listeningTo.clear();
		listeningTo = null;
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		setInput(oldInput, newInput);
	}
}
