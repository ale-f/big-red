package dk.itu.big_red.utilities.ui.jface;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class WorkspaceProvider
		implements ITreeContentProvider, IResourceChangeListener {
	private Viewer viewer;
	private IResource input;
	
	@Override
	public void dispose() {
		if (input != null)
			input.getWorkspace().removeResourceChangeListener(this);
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		if (oldInput instanceof IResource)
			((IResource)oldInput).getWorkspace().
				removeResourceChangeListener(this);
		if (newInput instanceof IResource) {
			input = (IResource)newInput;
			input.getWorkspace().addResourceChangeListener(this);
		} else input = null;
	}

	protected Viewer getViewer() {
		return viewer;
	}
	
	private static final Object[] NONE = new Object[0];
	
	private Object[] getMembers(IContainer c) {
		if (c instanceof IProject && !((IProject)c).isOpen())
			return NONE;
		try {
			return c.members(0);
		} catch (CoreException e) {
			e.printStackTrace();
			return NONE;
		}
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IContainer) {
			return getMembers((IContainer)inputElement);
		} else return NONE;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IResource) {
			return ((IResource)element).getParent();
		} else return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IContainer) {
			return getMembers((IContainer)element).length != 0;
		} else return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		getViewer().refresh();
	}
}
