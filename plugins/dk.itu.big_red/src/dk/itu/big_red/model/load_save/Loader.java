package dk.itu.big_red.model.load_save;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

import dk.itu.big_red.model.ModelObject;

/**
 * Classes extending Loader can read objects from an {@link InputStream}.
 * @see Saver
 * @author alec
 */
public abstract class Loader {
	public static final String EXTENSION_POINT = "dk.itu.big_red.import";
	
	protected InputStream source = null;
	
	/**
	 * Sets the source of the import to the given {@link InputStream}. The
	 * InputStream will be closed once the input has been read.
	 * @param is an InputStream
	 * @return <code>this</code>, for convenience
	 */
	public Loader setInputStream(InputStream is) {
		if (is != null)
			source = is;
		return this;
	}
	
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
	 * Indicates whether or not the model is ready to be imported.
	 * @return <code>true</code> if the model is ready to be imported, or
	 *         <code>false</code> otherwise
	 */
	public boolean canImport() {
		return (source != null);
	}
	
	/**
	 * Imports the object. This function should not be called unless {@link
	 * Loader#canImport canImport} returns <code>true</code>.
	 * @throws LoadFailedException if the import failed
	 */
	public abstract ModelObject importObject() throws LoadFailedException;
	
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
	 */
	public static ModelObject fromFile(IFile f) throws LoadFailedException {
		IContentType ct;
		try {
			ct = f.getContentDescription().getContentType();
		} catch (CoreException e) {
			ct = null;
		}
		if (ct == null)
			return null;
		for (IConfigurationElement ice :
			RegistryFactory.getRegistry().
				getConfigurationElementsFor(EXTENSION_POINT)) {
			if (ct.getId().equals(ice.getAttribute("contentType"))) {
				Loader i;
				try {
					i = (Loader)ice.createExecutableExtension("class");
					i.setFile(f).setInputStream(f.getContents());
				} catch (CoreException e) {
					return null;
				}
				if (i.canImport()) {
					return i.importObject();
				} else return null;
			}
		}
		return null;
	}
	
	private ArrayList<IStatus> notices;
	
	protected void addNotice(IStatus status) {
		if (notices == null)
			notices = new ArrayList<IStatus>();
		notices.add(status);
	}
	
	public List<IStatus> getNotices() {
		return notices;
	}
}