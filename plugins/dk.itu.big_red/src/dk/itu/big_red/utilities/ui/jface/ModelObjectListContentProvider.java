package dk.itu.big_red.utilities.ui.jface;

import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;

public abstract class ModelObjectListContentProvider
		extends ModelObjectContentProvider
		implements IStructuredContentProvider {
	private AbstractListViewer alv;
	
	public ModelObjectListContentProvider(AbstractListViewer alv) {
		this.alv = alv;
	}
	
	protected AbstractListViewer getViewer() {
		return alv;
	}
}
