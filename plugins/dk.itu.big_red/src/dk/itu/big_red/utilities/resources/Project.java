package dk.itu.big_red.utilities.resources;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;

import dk.itu.big_red.builder.BigraphBuilder;
import dk.itu.big_red.builder.BigraphNature;
import dk.itu.big_red.utilities.ui.UI;

/**
 * Utility functions for manipulating an Eclipse {@link IProject project} and
 * the {@link IResource resources} they contain.
 * @author alec
 */
public final class Project {
	private Project() {}
	
	/**
	 * Returns the workspace.
	 * @return the workspace
	 * @see ResourcesPlugin#getWorkspace()
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	/**
	 * Returns the workspace root.
	 * @return the workspace root
	 * @see IWorkspace#getRoot()
	 */
	public static IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
	}
	
	/**
	 * Returns the workspace's rule factory.
	 * @return an {@link IResourceRuleFactory}
	 * @see IWorkspace#getRuleFactory()
	 */
	public static IResourceRuleFactory getRuleFactory() {
		return getWorkspace().getRuleFactory();
	}
	
	public static IProjectDescription newBigraphProjectDescription(
			String projectName) {
		IProjectDescription desc =
				getWorkspace().newProjectDescription(projectName);
		ICommand cmd = desc.newCommand();
		cmd.setBuilderName(BigraphBuilder.BUILDER_ID);
		desc.setNatureIds(new String[] { BigraphNature.NATURE_ID });
		desc.setBuildSpec(new ICommand[] { cmd });
		return desc;
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
	 * Classes implementing <strong>IWorkspaceModification</strong> can be
	 * executed by a {@link ModificationRunner} to make changes to the Eclipse
	 * workspace.
	 * @author alec
	 */
	public interface IWorkspaceModification {
		/**
		 * Returns the scheduling rule required by this modification.
		 * @return an {@link ISchedulingRule}; can be <code>null</code>
		 * @see Project#getRuleFactory()
		 */
		ISchedulingRule getSchedulingRule();
		
		/**
		 * Executes this modification.
		 * @param monitor an {@link IProgressMonitor}; will not be
		 * <code>null</code>
		 * @throws CoreException if the modification fails
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
		private final IProjectDescription description;
		
		public CreateProject(IProject project) {
			this(project, null);
		}
		
		public CreateProject(
				IProject project, IProjectDescription description) {
			this.project = project;
			this.description = description;
		}
		
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.subTask("Creating project");
			project.create(description, monitor);
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
	
	private static abstract class MarkerModification
			implements IWorkspaceModification {
		private final IResource resource;
		
		public MarkerModification(IResource resource) {
			this.resource = resource;
		}
		
		protected IResource getResource() {
			return resource;
		}
		
		@Override
		public ISchedulingRule getSchedulingRule() {
			return getRuleFactory().markerRule(resource);
		}
	}
	
	public static class CreateMarker extends MarkerModification {
		private final String type;
		private final Map<String, Object> attributes =
				new HashMap<String, Object>();
		
		public CreateMarker(IResource resource, String type) {
			super(resource);
			this.type = type;
		}
		
		public CreateMarker addAttribute(String attributeName, Object value) {
			if (attributeName != null && value != null)
				attributes.put(attributeName, value);
			return this;
		}
		
		private IMarker marker;
		
		public IMarker getMarker() {
			return marker;
		}
		
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.subTask("Marking " + getResource());
			marker = getResource().createMarker(type);
			for (Entry<String, Object> attr : attributes.entrySet())
				marker.setAttribute(attr.getKey(), attr.getValue());
		}
	}
	
	public static class DeleteMarker extends MarkerModification {
		private final IMarker marker;
		
		public DeleteMarker(IResource resource, IMarker marker) {
			super(resource);
			this.marker = marker;
		}
		
		@Override
		public void run(IProgressMonitor monitor) throws CoreException {
			marker.delete();
		}
	}
	
	/**
	 * <strong>ModificationRunner</strong> is a wrapper class which executes
	 * {@link IWorkspaceModification}s safely.
	 * @author alec
	 */
	public static final class ModificationRunner extends WorkspaceJob {
		/**
		 * Classes extending <strong>Callback</strong> will be notified when
		 * something interesting happens to a {@link ModificationRunner}.
		 * <p>Note that {@link ModificationRunner} will always arrange for
		 * these methods to be called as part of the event loop (in the UI
		 * thread).
		 * @author alec
		 */
		public abstract static class Callback {
			/**
			 * Called when a {@link ModificationRunner} successfully completes
			 * all jobs.
			 */
			public void onSuccess() {}
			
			/**
			 * Called when a {@link ModificationRunner} has not completed
			 * successfully due to:&mdash;
			 * <ul>
			 * <li>an exception being thrown by one of its {@link
			 * IWorkspaceModification}s; or
			 * <li>a cancellation request from the user (in which case
			 * <code>e</code> will be an {@link OperationCanceledException}).
			 * </ul>
			 * @param e a {@link CoreException}
			 */
			public void onError(CoreException e) {}
			
			/**
			 * Called when {@link
			 * ModificationRunner#runInWorkspace(IProgressMonitor)} completes,
			 * regardless of the outcome.
			 */
			public void always() {}
		}

		private final Callback payload;
		private final
			Collection<? extends IWorkspaceModification> modifications;
		
		public ModificationRunner(
				Callback payload, IWorkspaceModification... modifications) {
			this(payload, Arrays.asList(modifications));
		}
		
		public ModificationRunner(
				Callback payload,
				Collection<? extends IWorkspaceModification> modifications) {
			super("Performing workspace update");
			this.payload = payload;
			this.modifications = modifications;
			
			ISchedulingRule result = null;
			for (IWorkspaceModification m : modifications)
				result = MultiRule.combine(result, m.getSchedulingRule());
			setRule(result);
		}
		
		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor)
				throws CoreException {
			if (monitor == null)
				monitor = new NullProgressMonitor();
			monitor.beginTask(
					"Performing workspace update", modifications.size());
			try {
				for (IWorkspaceModification j : modifications) {
					if (monitor.isCanceled())
						break;
					j.run(monitor);
					monitor.worked(1);
				}
				
				if (payload != null && !monitor.isCanceled())
					UI.asyncExec(new Runnable() {
						@Override
						public void run() {
							payload.onSuccess();
						}
					});
				
				if (!monitor.isCanceled()) {
					return Status.OK_STATUS;
				} else throw new OperationCanceledException();
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
	 * @return the (scheduled) {@link ModificationRunner} encapsulating the
	 * required workspace operations
	 * @throws CoreException if the file couldn't be created or modified
	 */
	public static ModificationRunner setContents(
			IFile file, InputStream contents, ModificationRunner.Callback r) {
		ModificationRunner mr = new ModificationRunner(r, (file.exists() ?
				new SetContents(file, contents) :
				new CreateFile(file, contents)));
		mr.schedule();
		return mr;
	}
	
	public static IResourceDelta getSpecificDelta(
			IResourceDelta rootDelta, IPath p) {
		if (rootDelta != null && p != null) {
			return rootDelta.findMember(p);
		} else return null;
	}
}
