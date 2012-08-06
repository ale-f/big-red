package org.bigraph.model.resources;

/**
 * Classes implementing <strong>IContainerWrapper</strong> wrap
 * <i>containers</i>: filesystem-like objects which have a path and which
 * contain other resources.
 * <p>This interface allows model loaders to evaluate relative paths portably.
 * @author alec
 * @see IFileWrapper
 * @see IResourceWrapper
 */
public interface IContainerWrapper extends IResourceWrapper {
	/**
	 * Returns the resource at the given relative path.
	 * @param path a path relative to this container
	 * @return an {@link IResourceWrapper}, or <code>null</code> if no resource
	 * was found at the given relative path
	 */
	IResourceWrapper getResource(String path);
}
