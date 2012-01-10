package dk.itu.big_red.utilities.resources;

import org.eclipse.core.resources.IFile;

/**
 * Classes implementing {@link IFileBackable} can have an associated {@link
 * IFile} which backs them on the filesystem.
 * @author alec
 *
 */
public interface IFileBackable {
	/**
	 * Returns the {@link IFile} which backs this object, if there is one.
	 * <p>There is no guarantee that the returned {@link IFile} will contain a
	 * current and accurate representation of this object's state, or that
	 * it necessarily backs <i>only</i> this object.
	 * @return an {@link IFile}, or <code>null</code>
	 */
	public IFile getFile();
	
	/**
	 * Sets the {@link IFile} which backs this object.
	 * @param file an {@link IFile}, or <code>null</code>
	 * @return <code>this</code>, for convenience
	 */
	public IFileBackable setFile(IFile file);
}
