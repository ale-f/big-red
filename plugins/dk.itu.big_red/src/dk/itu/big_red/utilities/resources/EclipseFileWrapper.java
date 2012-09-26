package dk.itu.big_red.utilities.resources;

import java.io.InputStream;

import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.Loader;
import org.bigraph.model.resources.IFileWrapper;
import org.bigraph.model.wrapper.LoaderUtilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;


/**
 * The <strong>EclipseFileWrapper</strong> class implements the {@link
 * IFileWrapper} interface on top of an {@link IFile}.
 * <p>Note that the {@link #load()} method is the recommended way to open a
 * file &mdash; it consults the extension registry to get the right loader and
 * to configure it with the appropriate participants.
 * @author alec
 */
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
			Loader l = LoaderUtilities.forContentType(ct);
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
	public InputStream open() {
		try {
			return getContents();
		} catch (LoadFailedException e) {
			return null;
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
