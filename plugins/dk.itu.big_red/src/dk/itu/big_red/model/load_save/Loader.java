package dk.itu.big_red.model.load_save;

import java.io.InputStream;
import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.LoadFailedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import dk.itu.big_red.utilities.resources.EclipseFileWrapper;

/**
 * Classes extending Loader can read objects from an {@link InputStream}.
 * @see Saver
 * @author alec
 */
public abstract class Loader extends org.bigraph.model.loaders.Loader {
	public static final String EXTENSION_POINT = "dk.itu.big_red.import";
	
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
	 * @throws CoreException never; for backwards compatibility
	 * @throws LoadFailedException if {@link #importObject()} fails
	 */
	public static ModelObject fromFile(IFile f)
			throws CoreException, LoadFailedException {
		return new EclipseFileWrapper(f).load();
	}
}