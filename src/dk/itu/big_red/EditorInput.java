package dk.itu.big_red;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class EditorInput implements IEditorInput, IAdaptable {

	public String name = null;
	
	public EditorInput(String name) {
		this.name = name;
	}
	
	@Override
	public boolean exists() {
		return (this.name != null);
	}

	public boolean equals(Object o) {
		if (!(o instanceof EditorInput))
			return false;
		else if (getName().equals("#empty"))
			return false;
		else return ((EditorInput)o).getName().equals(getName());
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return ImageDescriptor.getMissingImageDescriptor();
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
