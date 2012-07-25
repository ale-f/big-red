package org.bigraph.model.resources;

/**
 * Classes implementing <strong>IResourceWrapper</strong> wrap
 * <i>resources</i>: filesystem-like objects which have a path.
 * <p>This interface allows model loaders to calculate relative paths portably.
 * @author alec
 * @see IFileWrapper
 * @see IContainerWrapper
 */
public interface IResourceWrapper {
	/**
	 * Returns the absolute path to this resource.
	 * <p>The precise format of an absolute path is not specified, but:&mdash;
	 * <ul>
	 * <li>it will use <code>/</code> as a path separator; and
	 * <li>it will not contain <code>.</code> or <code>..</code> components.
	 * </ul>
	 * @return the absolute path to this resource
	 * @see #getRelativePath(String)
	 */
	public String getPath();
	
	/**
	 * Returns a path to this resource relative to the given absolute path.
	 * @param relativeTo the absolute path of a container
	 * @return a relative path to this resource
	 * @see #getPath()
	 * @see IContainerWrapper
	 */
	public String getRelativePath(String relativeTo);
	
	/**
	 * Returns the parent container of this resource.
	 * @return an {@link IContainerWrapper}, or <code>null</code> if this
	 * resource is the root
	 */
	public IContainerWrapper getParent();
}
