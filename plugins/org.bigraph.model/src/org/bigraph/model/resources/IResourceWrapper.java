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
	 * Returns this resource's name (the last component of its path).
	 * @return this resource's name
	 */
	String getName();
	
	/**
	 * Returns the absolute path to this resource.
	 * <p>The precise format of a path is not specified, but:&mdash;
	 * <ul>
	 * <li>the path separator will be the <code>/</code> character;
	 * <li>absolute paths will not contain <code>.</code> or <code>..</code>
	 * components; and
	 * <li>relative paths will not begin with the <code>/</code> character.
	 * </ul>
	 * @return the absolute path to this resource
	 * @see #getRelativePath(String)
	 */
	String getPath();
	
	/**
	 * Returns a path to this resource relative to the given absolute path.
	 * @param relativeTo the absolute path of a container
	 * @return a relative path to this resource
	 * @see #getPath()
	 * @see IContainerWrapper
	 */
	String getRelativePath(String relativeTo);
	
	/**
	 * Returns the parent container of this resource.
	 * @return an {@link IContainerWrapper}, or <code>null</code> if this
	 * resource is the root
	 */
	IContainerWrapper getParent();
}
