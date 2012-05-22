package it.uniud.bigredit.model.load_save.loaders;

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
	
	public static IResourceDelta getSpecificDelta(IResourceDelta rootDelta, IResource r) {
		return getSpecificDelta(rootDelta, r.getFullPath());
	}
	
	public static IResourceDelta getSpecificDelta(IResourceDelta rootDelta, IPath p) {
		if (rootDelta != null) {
			return rootDelta.findMember(p);
		} else return null;
	}
}
