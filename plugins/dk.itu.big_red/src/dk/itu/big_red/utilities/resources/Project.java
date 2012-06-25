package dk.itu.big_red.utilities.resources;

import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.ui.UI;

/**
 * Utility functions for manipulating an Eclipse {@link IProject project} and
 * the {@link IResource resources} they contain.
 * @author alec
 *
 */
public final class Project {
	private Project() {}
	
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	/**
	 * Gets the workspace root.
	 * @return the workspace root
	 */
	public static IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}
	
	public static IResourceRuleFactory getRuleFactory() {
		return getWorkspace().getRuleFactory();
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
	
	public static class SaveRunnable {
		public void onSuccess() {}
		public void always() {}
	}
	
	private static final class SaveJob extends WorkspaceJob {
		private IFile file;
		private InputStream contents;
		private SaveRunnable payload;
		
		private SaveJob(IFile file, InputStream contents, SaveRunnable payload) {
			super("Setting contents");
			this.file = file;
			this.contents = contents;
			this.payload = payload;
		}
		
		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor)
				throws CoreException {
			try {
				if (file.exists()) {
					file.setContents(contents, 0, monitor);
				} else file.create(contents, 0, monitor);
				
				if (payload != null)
					UI.asyncExec(new Runnable() {
						@Override
						public void run() {
							payload.onSuccess();
						}
					});
				return Status.OK_STATUS;
			} finally {
				if (payload != null)
					UI.asyncExec(new Runnable() {
						@Override
						public void run() {
							payload.always();
						}
					});
			}
		}
	}
	
	/**
	 * Sets the contents of the given {@link IFile} (which need not already
	 * exist).
	 * @param file an {@link IFile}
	 * @param contents an {@link InputStream} specifying its contents
	 * @param r a {@link Runnable} to be executed in the UI thread when the
	 * operation has completed
	 * @throws CoreException if the file couldn't be created or modified
	 */
	public static void setContents(
			IFile file, InputStream contents, SaveRunnable r) {
		SaveJob j = new SaveJob(file, contents, r);
		j.setRule(getRuleFactory().modifyRule(file));
		j.schedule();
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
