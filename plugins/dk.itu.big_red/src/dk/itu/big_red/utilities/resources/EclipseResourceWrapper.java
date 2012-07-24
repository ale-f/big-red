package dk.itu.big_red.utilities.resources;

import org.bigraph.model.resources.IContainerWrapper;
import org.bigraph.model.resources.IResourceWrapper;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;

abstract class EclipseResourceWrapper implements IResourceWrapper {
	protected abstract IResource getResource();
	
	@Override
	public String getPath() {
		return getResource().getFullPath().toString();
	}

	@Override
	public String getRelativePath(String relativeTo) {
		return getResource().getFullPath().makeRelativeTo(
				new Path(relativeTo)).toString();
	}
	
	@Override
	public IContainerWrapper getParent() {
		IContainer parent = getResource().getParent();
		return (parent != null ? new EclipseContainerWrapper(parent) : null);
	}
}
