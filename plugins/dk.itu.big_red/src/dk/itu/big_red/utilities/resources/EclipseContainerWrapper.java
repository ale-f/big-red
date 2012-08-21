package dk.itu.big_red.utilities.resources;

import org.bigraph.model.resources.IContainerWrapper;
import org.bigraph.model.resources.IResourceWrapper;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

/**
 * The <strong>EclipseContainerWrapper</strong> class implements the {@link
 * IContainerWrapper} interface on top of an {@link IContainer}.
 * @author alec
 */
public class EclipseContainerWrapper extends EclipseResourceWrapper
		implements IContainerWrapper {
	private final IContainer container;
	
	@Override
	public IContainer getResource() {
		return container;
	}
	
	public EclipseContainerWrapper(IContainer container) {
		this.container = container;
	}

	@Override
	public IResourceWrapper getResource(String path) {
		IResource r = getResource().findMember(path);
		if (r instanceof IContainer) {
			return new EclipseContainerWrapper((IContainer)r);
		} else if (r instanceof IFile) {
			return new EclipseFileWrapper((IFile)r);
		} else return null;
	}
}
