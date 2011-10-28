package dk.itu.big_red.util.resources;

import org.eclipse.core.resources.IFile;

public interface IFileBackable {
	public IFile getFile();
	
	/**
	 * @return <code>this</code>, for convenience
	 */
	public IFileBackable setFile(IFile file);
}
