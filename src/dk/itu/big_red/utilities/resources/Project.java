package dk.itu.big_red.utilities.resources;

import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import dk.itu.big_red.utilities.io.IOAdapter;

/**
 * Utility functions for manipulating an Eclipse {@link IProject project} and
 * the {@link IResource resources} they contain.
 * @author alec
 *
 */
public final class Project {
	private Project() {}
	
	/**
	 * Gets the workspace root.
	 * @return the workspace root
	 */
	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/**
	 * Gets an {@link IFolder} contained by <code>c</code>. If it doesn't exist
	 * already, then it's created.
	 * @param c the parent {@link IContainer}
	 * @param name the folder's name
	 * @return an IFolder guaranteed to exist on the local filesystem
	 * @throws CoreException if {@link IFolder#create(int, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)} goes wrong
	 */
	public static IFolder getFolder(IContainer c, String name) throws CoreException {
		IFolder f = c.getFolder(new Path(name));
		if (!f.exists())
			f.create(0, true, null);
		return f;
	}
	
	/**
	 * Indicates whether or not the named folder exists as a child of
	 * <code>c</code>.
	 * @param c the parent {@link IContainer}
	 * @param name the folder's name
	 * @return whether the folder exists or not
	 */
	public static boolean folderExists(IContainer c, String name) {
		return c.getFolder(new Path(name)).exists();
	}
	
	/**
	 * Gets an {@link IFile} contained by <code>c</code>. If it doesn't exist
	 * already, then it's created (as an empty file).
	 * @param c the parent {@link IContainer}
	 * @param name the file's name
	 * @return an IFile guaranteed to exist on the local filesystem
	 * @throws CoreException if {@link IFile#create(java.io.InputStream,
	 * boolean, org.eclipse.core.runtime.IProgressMonitor)} goes wrong
	 */
	public static IFile getFile(IContainer c, String name) throws CoreException {
		IFile f = c.getFile(new Path(name));
		if (!f.exists())
			f.create(IOAdapter.getNullInputStream(), true, null);
		return f;
	}
	
	/**
	 * Indicates whether or not the named file exists as a child of
	 * <code>c</code>.
	 * @param c the parent {@link IContainer}
	 * @param name the file's name
	 * @return whether the file exists or not
	 */
	public static boolean fileExists(IContainer c, String name) {
		return c.getFile(new Path(name)).exists();
	}
	
	/**
	 * Indicates whether or not <code>c</code> contains a resource with the
	 * given path.
	 * @param c an {@link IContainer}
	 * @param path the path of a would-be child
	 * @return whether the path identifies an extant resource or not
	 */
	public static boolean pathExists(IContainer c, IPath path) {
		return (c.findMember(path) != null);
	}
	
	/**
	 * Gets a resource with the given path (relative to <code>c</code>). Note
	 * that this method will <i>not</i> create the resource if it doesn't
	 * already exist.
	 * @param c the container to search in, or - if <code>null</code> - the workspace root
	 * @param path the path of a would-be resource
	 * @return the {@link IResource} requested, or <code>null</code> if it
	 * doesn't exist
	 */
	public static IResource findResourceByPath(IContainer c, IPath path) {
		if (c == null)
			c = getWorkspaceRoot();
		return c.findMember(path);
	}
	
	/**
	 * Gets a container with the given path (relative to <code>c</code>). Note
	 * that this method will <i>not</i> create the container if it doesn't
	 * already exist.
	 * @param c the container to search in, or - if <code>null</code> - the workspace root
	 * @param path the path of a would-be container
	 * @return the {@link IContainer} requested, or <code>null</code> if it
	 * doesn't exist
	 * @see Project#findResourceByPath
	 */
	public static IContainer findContainerByPath(IContainer c, IPath path) {
		IResource r = findResourceByPath(c, path);
		return (r instanceof IContainer ? (IContainer)r : null);
	}
	
	/**
	 * Gets a file with the given path (relative to <code>c</code>). Note that
	 * this method will <i>not</i> create the file if it doesn't already exist.
	 * @param c the container to search in, or - if <code>null</code> - the workspace root
	 * @param path the path of a would-be file
	 * @return the {@link IFile} requested, or <code>null</code> if it doesn't
	 * exist
	 */
	public static IFile findFileByPath(IContainer c, IPath path) {
		IResource r = findResourceByPath(c, path);
		return (r instanceof IFile ? (IFile)r : null);
	}
	
	/**
	 * Indicates whether or not the two {@link IFile}s provided belong to the
	 * same {@link IProject}.
	 * @param one the first {@link IFile} to compare
	 * @param two the second {@link IFile} to compare
	 * @return whether the two IFiles belong to the same project or not
	 */
	public static boolean compareProjects(IFile one, IFile two) {
		return (one != null && two != null &&
				one.getProject().equals(two.getProject()));
	}
	
	public static IFile getWorkspaceFile(IPath p) {
		return getWorkspaceRoot().getFile(p);
	}
	
	public static IFolder getWorkspaceFolder(IPath p) {
		return getWorkspaceRoot().getFolder(p);
	}
	
	/**
	 * Sets the contents of the given {@link IFile} (which need not already
	 * exist).
	 * @param file an {@link IFile}
	 * @param contents an {@link InputStream} specifying its contents
	 * @throws CoreException if the file couldn't be created or modified
	 */
	public static void setContents(IFile file, InputStream contents) throws CoreException {
		if (file.exists()) {
			file.setContents(contents, 0, null);
		} else file.create(contents, 0, null);
	}
	
	public static IPath getRelativePath(IResource relativeTo, IResource resource) {
		IPath relativeToContainer = null;
		if (relativeTo instanceof IContainer) {
			relativeToContainer = relativeTo.getFullPath();
		} else if (relativeTo instanceof IFile) {
			relativeToContainer = relativeTo.getParent().getFullPath();
		}
		
		if (relativeToContainer != null) {
			return resource.getFullPath().makeRelativeTo(relativeToContainer);
		} else return resource.getFullPath();
	}
	
	public static IResourceDelta getSpecificDelta(IResourceDelta rootDelta, IResource r) {
		return getSpecificDelta(rootDelta, r.getFullPath());
	}
	
	public static IResourceDelta getSpecificDelta(IResourceDelta rootDelta, IPath p) {
		if (rootDelta != null) {
			return rootDelta.findMember(p);
		} else return null;
	}
}
