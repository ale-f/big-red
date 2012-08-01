package dk.itu.big_red.utilities.resources;

import java.io.InputStream;

import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.Loader;
import org.bigraph.model.resources.IFileWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;

import dk.itu.big_red.model.load_save.LoaderUtilities;

public class EclipseFileWrapper extends EclipseResourceWrapper
		implements IFileWrapper {
	private final IFile file;

	@Override
	public IFile getResource() {
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
			Loader l = LoaderUtilities.newLoaderFor(ct);
			if (l != null) {
				l.setFile(this).setInputStream(getContents());
				if (l.canImport()) {
					return l.importObject();
				} else throw new LoadFailedException("The loader for " +
						file + " could not be configured");
			} else throw new LoadFailedException(
					"No loader was found for " + getResource());
		} catch (CoreException ce) {
			throw new LoadFailedException(ce);
		}
	}
	
	@Override
	public InputStream getContents() throws LoadFailedException {
		try {
			return getResource().getContents();
		} catch (CoreException ce) {
			throw new LoadFailedException(ce);
		}
	}
}
