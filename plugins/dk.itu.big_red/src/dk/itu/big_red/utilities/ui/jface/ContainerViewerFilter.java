package dk.itu.big_red.utilities.ui.jface;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * ContainerViewerFilters filter {@link IResource} trees to hide files.
 * @author alec
 */
public class ContainerViewerFilter extends ViewerFilter {
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IContainer)
			return true;
		else return false;
	}
}
