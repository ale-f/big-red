package dk.itu.big_red.utilities.resources;

import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.resources.IFileWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

import dk.itu.big_red.model.load_save.Loader;

public class EclipseFileWrapper extends EclipseResourceWrapper
		implements IFileWrapper {
	private final IFile file;

	@Override
	protected IFile getResource() {
		return file;
	}
	
	public EclipseFileWrapper(IFile file) {
		this.file = file;
	}

	@Override
	public ModelObject load() throws LoadFailedException {
		try {
			IContentType ct =
					getResource().getContentDescription().getContentType();
			for (IConfigurationElement ice :
				RegistryFactory.getRegistry().
					getConfigurationElementsFor(Loader.EXTENSION_POINT)) {
				if (ct.getId().equals(ice.getAttribute("contentType"))) {
					Loader i = (Loader)ice.createExecutableExtension("class");
					i.setFile(getResource()).setInputStream(
							getResource().getContents());
					if (i.canImport()) {
						return i.importObject();
					} else {
						throw new LoadFailedException("What?");
					}
				}
			}
		} catch (CoreException ce) {
			throw new LoadFailedException(ce);
		}
		return null;
	}
}
