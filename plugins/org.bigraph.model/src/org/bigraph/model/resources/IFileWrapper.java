package org.bigraph.model.resources;

import java.io.InputStream;

import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.LoadFailedException;

/**
 * Classes implementing <strong>IFileWrapper</strong> wrap <i>files</i>:
 * filesystem-like objects which have a path and which contain data.
 * <p>This interface allows model loaders to load dependencies portably.
 * @author alec
 * @see IFileWrapper
 * @see IResourceWrapper
 */
public interface IFileWrapper extends IResourceWrapper, IOpenable {
	/**
	 * Loads the contents of this file as a {@link ModelObject}.
	 * @return a {@link ModelObject} (not <code>null</code>)
	 * @throws LoadFailedException if something goes wrong
	 */
	ModelObject load() throws LoadFailedException;
	
	/**
	 * Produces an {@link InputStream} representing the contents of this file.
	 * @return an {@link InputStream} (not <code>null</code>)
	 * @throws LoadFailedException if something goes wrong
	 */
	InputStream getContents() throws LoadFailedException;
}
