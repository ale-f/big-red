package dk.itu.big_red.utilities.ui.jface;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

public class WorkspaceProvider extends LabelProvider
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

	@Override
	public Image getImage(Object element) {
		ISharedImages im = PlatformUI.getWorkbench().getSharedImages();
		if (element instanceof IProject) {
			return im.getImage(
				((IProject)element).isOpen() ?
					SharedImages.IMG_OBJ_PROJECT :
					SharedImages.IMG_OBJ_PROJECT_CLOSED);
		} else if (element instanceof IContainer) {
			return im.getImage(ISharedImages.IMG_OBJ_FOLDER);
		} else if (element instanceof IFile) {
			return im.getImage(ISharedImages.IMG_OBJ_FILE);
		} else return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IResource) {
			return ((IResource)element).getName();
		} else return null;
	}
}
