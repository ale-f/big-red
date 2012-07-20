package dk.itu.big_red.model.load_save;

import java.io.InputStream;
import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.LoadFailedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Classes extending Loader can read objects from an {@link InputStream}.
 * @see Saver
 * @author alec
 */
public abstract class Loader extends org.bigraph.model.loaders.Loader {
	public static final String EXTENSION_POINT = "dk.itu.big_red.import";
	
	private IFile file;
	
	/**
	 * Associates an {@link IFile} with this {@link Loader}. (The file will
	 * <i>not</i> be automatically opened by this method &mdash; {@link
	 * #setInputStream(InputStream)} must be called separately.)
	 * @param file an {@link IFile}
	 * @return <code>this</code>, for convenience
	 */
	public Loader setFile(IFile file) {
		this.file = file;
		return this;
	}
	
	/**
	 * Returns the {@link IFile} associated with this {@link Loader}, if there
	 * is one.
	 * @return an {@link IFile}, or <code>null</code>
	 */
	public IFile getFile() {
		return file;
	}
	
	/**
	 * Loads an object from an {@link IFile} by:
	 * <ul>
	 * <li>getting its content type;
	 * <li>finding an importer for that content type registered with the
	 * <code>{@value #EXTENSION_POINT}</code> extension point; and
	 * <li>instantiating that importer, passing it the {@link IFile}, and
	 * calling {@link #importObject()}.
	 * </ul>
	 * @param f an {@link IFile}
	 * @return an object, or <code>null</code>
	 * @throws LoadFailedException if {@link #importObject()} fails
	 * @throws CoreException if an Eclipse method fails
	 */
	public static ModelObject fromFile(IFile f)
			throws CoreException, LoadFailedException {
		IContentType ct = f.getContentDescription().getContentType();
		for (IConfigurationElement ice :
			RegistryFactory.getRegistry().
				getConfigurationElementsFor(EXTENSION_POINT)) {
			if (ct.getId().equals(ice.getAttribute("contentType"))) {
				Loader i = (Loader)ice.createExecutableExtension("class");
				i.setFile(f).setInputStream(f.getContents());
				if (i.canImport()) {
					return i.importObject();
				} else {
					throw new LoadFailedException("What?");
				}
			}
		}
		return null;
	}
}