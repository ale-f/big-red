package org.bigraph.model.resources;

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
public interface IFileWrapper extends IResourceWrapper {
	/**
	 * Attempts to load the contents of this file as a {@link ModelObject}.
	 * @return a {@link ModelObject}
	 * @throws LoadFailedException if something goes wrong
	 */
	public ModelObject load() throws LoadFailedException;
}
