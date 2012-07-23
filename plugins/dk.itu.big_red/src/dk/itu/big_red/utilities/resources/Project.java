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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import dk.itu.big_red.utilities.ui.UI;

/**
 * Utility functions for manipulating an Eclipse {@link IProject project} and
 * the {@link IResource resources} they contain.
 * @author alec
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
	
	public interface IWorkspaceModification {
		ISchedulingRule getSchedulingRule();
		/**
		 * @param monitor an {@link IProgressMonitor} (not <code>null</code>)
		 * @throws CoreException
		 */
		void run(IProgressMonitor monitor) throws CoreException;
	}
	
	public static class SetContents implements IWorkspaceModification {
		private final IFile file;
		private final InputStream contents;
		
		public SetContents(IFile file, InputStream contents) {
			this.file = file;
			this.contents = contents;
		}
		
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.subTask("Setting contents");
			file.setContents(contents, IResource.FORCE, monitor);
		}
		
		@Override
		public ISchedulingRule getSchedulingRule() {
			return getRuleFactory().modifyRule(file);
		}
	}
	
	public static class CreateFile implements IWorkspaceModification {
		private final IFile file;
		private final InputStream contents;
		
		public CreateFile(IFile file, InputStream contents) {
			this.file = file;
			this.contents = contents;
		}
		
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.subTask("Creating file");
			file.create(contents, IResource.FORCE, monitor);
		}
		
		@Override
		public ISchedulingRule getSchedulingRule() {
			return getRuleFactory().createRule(file);
		}
	}
	
	public static class CreateFolder implements IWorkspaceModification {
		private final IFolder folder;
		
		public CreateFolder(IFolder folder) {
			this.folder = folder;
		}
		
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.subTask("Creating folder");
			folder.create(0, true, monitor);
		}
		
		@Override
		public ISchedulingRule getSchedulingRule() {
			return getRuleFactory().createRule(folder);
		}
	}
	
	public static class CreateProject implements IWorkspaceModification {
		private final IProject project;
		
		public CreateProject(IProject project) {
			this.project = project;
		}
		
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.subTask("Creating project");
			project.create(monitor);
		}
		
		@Override
		public ISchedulingRule getSchedulingRule() {
			return getRuleFactory().createRule(project);
		}
	}
	
	public static class OpenProject implements IWorkspaceModification {
		private final IProject project;
		
		public OpenProject(IProject project) {
			this.project = project;
		}
		
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.subTask("Opening project");
			project.open(monitor);
		}
		
		@Override
		public ISchedulingRule getSchedulingRule() {
			return getRuleFactory().modifyRule(project);
		}
	}
	
	private static IWorkspaceModification getSetModification(
			IFile file, InputStream contents) {
		return (file.exists() ?
				new SetContents(file, contents) :
					new CreateFile(file, contents));
	}
	
	public static final class ModificationRunner extends WorkspaceJob {
		public static class Callback {
			public void onSuccess() {}
			public void onError(CoreException e) {}
			public void onCancel() {}
			public void always() {}
		}

		private IWorkspaceModification modifications[];
		private Callback payload;
		
		public ModificationRunner(
				Callback payload, IWorkspaceModification... modifications) {
			super("Performing workspace update");
			this.modifications = modifications;
			this.payload = payload;
			
			ISchedulingRule schedulingRules[] =
					new ISchedulingRule[modifications.length];
			for (int i = 0; i < modifications.length; i++)
				schedulingRules[i] = modifications[i].getSchedulingRule();
			setRule(MultiRule.combine(schedulingRules));
		}
		
		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor)
				throws CoreException {
			if (monitor == null)
				monitor = new NullProgressMonitor();
			try {
				for (IWorkspaceModification j : modifications) {
					if (monitor.isCanceled())
						break;
					j.run(monitor);
				}
				
				if (payload != null) {
					if (!monitor.isCanceled()) {
						UI.asyncExec(new Runnable() {
							@Override
							public void run() {
								payload.onSuccess();
							}
						});
					} else {
						UI.asyncExec(new Runnable() {
							@Override
							public void run() {
								payload.onCancel();
							}
						});
					}
				}
				return Status.OK_STATUS;
			} catch (final CoreException e) {
				if (payload != null)
					UI.asyncExec(new Runnable() {
						@Override
						public void run() {
							payload.onError(e);
						}
					});
				throw e;
			} finally {
				if (payload != null)
					UI.asyncExec(new Runnable() {
						@Override
						public void run() {
							payload.always();
						}
					});
				monitor.done();
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
			IFile file, InputStream contents, ModificationRunner.Callback r) {
		new ModificationRunner(r,
				getSetModification(file, contents)).schedule();
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
