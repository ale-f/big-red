package dk.itu.big_red.model.load_save;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.content.IContentType;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.utilities.resources.IFileBackable;
import dk.itu.big_red.utilities.resources.Types;


/**
 * Classes extending Loader can read objects from an {@link InputStream}.
 * 
 * <p>The existence of an Loader class for a given format implies that a
 * corresponding {@link Saver} class <i>should</i> exist for that format.
 * @see Saver
 * @author alec
 *
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
		IContentType ct = Types.findContentTypeFor(f);
		for (IConfigurationElement ice :
			RedPlugin.getConfigurationElementsFor(EXTENSION_POINT)) {
			if (ct.getId().equals(ice.getAttribute("contentType"))) {
				Loader i = (Loader)RedPlugin.instantiate(ice);
				try {
					i.setInputStream(f.getContents());
				} catch (CoreException e) {
					return null;
				}
				/*
				 * Importers implementing IFileBackable indicate that they need
				 * the file as part of the import process (i.e., to resolve
				 * relative paths).
				 */
				if (i instanceof IFileBackable)
					((IFileBackable)i).setFile(f);
				if (i.canImport()) {
					return i.importObject();
				} else return null;
			}
		}
		return null;
	}
}