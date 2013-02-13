package dk.itu.big_red.editors;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.savers.SaveFailedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.editors.actions.RedoProxyAction;
import dk.itu.big_red.editors.actions.RevertProxyAction;
import dk.itu.big_red.editors.actions.UndoProxyAction;
import dk.itu.big_red.editors.actions.ProxyAction.IActionImplementor;
import dk.itu.big_red.editors.assistants.EditorError;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.resources.Project.ModificationRunner;
import dk.itu.big_red.utilities.ui.UI;

public abstract class AbstractEditor extends EditorPart
		implements IResourceChangeListener, IActionImplementor {
	public AbstractEditor() {
		ResourcesPlugin.getWorkspace().
			addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
	}

	private List<String> stateActions;
	
	/**
	 * Returns the list of <i>state actions</i>, those actions which want to be
	 * updated when the state of the model object is reversibly modified.
	 * @return a list of action IDs
	 * @see #updateActions(List)
	 */
	protected List<String> getStateActions() {
		if (stateActions == null)
			stateActions = new ArrayList<String>();
		return stateActions;
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}
	
	@Override
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		
		if (getModel() != null)
			getModel().dispose();
		
		if (actionRegistry != null) {
			actionRegistry.dispose();
			actionRegistry = null;
		}
		
		if (stateActions != null) {
			stateActions.clear();
			stateActions = null;
		}
		
		if (interestingResources != null) {
			interestingResources.clear();
			interestingResources = null;
		}
		
		super.dispose();
	}
	
	/**
	 * Registers a number of {@link IAction}s with the given {@link
	 * ActionRegistry}, optionally copying their IDs into a {@link List}.
	 * @param registry an {@link ActionRegistry}
	 * @param idList a list to be filled with {@link String} IDs; can be
	 * <code>null</code>
	 * @param actions a number of {@link IAction}s
	 * @return <code>actions</code>, for convenience
	 */
	public IAction[] registerActions(List<String> idList, IAction... actions) {
		ActionRegistry registry = getActionRegistry();
		for (IAction i : actions) {
			registry.registerAction(i);
			if (idList != null)
				idList.add(i.getId());
		}
		return actions;
	}

	private ActionRegistry actionRegistry;
	
	/**
	 * Returns this editor's {@link ActionRegistry}, creating and initialising
	 * it if necessary.
	 * @return a (possibly newly-initialised!) {@link ActionRegistry}
	 * @see #initializeActionRegistry()
	 */
	protected ActionRegistry getActionRegistry() {
		if (actionRegistry == null) {
			actionRegistry = new ActionRegistry();
			initializeActionRegistry();
		}
		return actionRegistry;
	}
	
	/**
	 * Calls {@link UpdateAction#update()} on the actions registered with the
	 * given IDs (if they <i>are</i> {@link UpdateAction}s, that is).
	 * @param actionIDs the list of IDs to update
	 */
	protected void updateActions(List<String> actionIDs) {
		ActionRegistry registry = getActionRegistry();
		for (String i : actionIDs) {
			IAction action = registry.getAction(i);
			if (action instanceof UpdateAction)
				((UpdateAction)action).update();
		}
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == ActionRegistry.class) {
			return getActionRegistry();
		} else return super.getAdapter(adapter);
	}
	
	/**
	 * Initialises the {@link ActionRegistry}. There's no need to call this
	 * method explicitly; the first call to {@link #getActionRegistry()} will
	 * do so automatically.
	 * @see #createActions()
	 */
	protected final void initializeActionRegistry() {
		setGlobalActionHandlers(registerActions(getStateActions(),
				new UndoProxyAction(this), new RedoProxyAction(this),
				new RevertProxyAction(this)));
		createActions();
	}
	
	private boolean saving;
	
	protected void setSaving(boolean saving) {
		this.saving = saving;
	}
	
	public boolean isSaving() {
		return saving;
	}
	
	/**
	 * Creates actions for this editor and registers them with the {@link
	 * ActionRegistry}.
	 */
	protected abstract void createActions();
	
	@Override
	protected void setInput(IEditorInput input) {
		IEditorInput current = getEditorInput();
		if (current instanceof FileEditorInput)
			removeInterestingResource(((FileEditorInput)current).getFile());
		super.setInput(input);
		setPartName(input.getName());
		if (input instanceof FileEditorInput)
			addInterestingResource(((FileEditorInput)input).getFile());
	}
	
	@Override
	protected void setInputWithNotify(IEditorInput input) {
		setInput(input);
        firePropertyChange(PROP_INPUT);
	}
	
	protected final boolean hasFocus() {
		return equals(getSite().getPage().getActivePart());
	}
	
	boolean promptOnFocus = false;
	private long modificationStamp = IResource.NULL_STAMP;
	
	protected boolean hasFileChanged() {
		long oldModificationStamp = modificationStamp;
		modificationStamp = getFile().getModificationStamp();
		if (oldModificationStamp != IResource.NULL_STAMP)
			if (oldModificationStamp != modificationStamp)
				return true;
		return false;
	}
	
	@Override
	public void setFocus() {
		if (hasFileChanged())
			onChange();
		if (promptOnFocus)
			promptToReplace();
	}
	
	private void onChange() {
		if (!getFile().isAccessible()) {
			getSite().getPage().closeEditor(this, false);
		} else if (getError() != null) {
			load();
		} else if (hasFocus()) {
			promptToReplace();
		} else if (!isSaving()) {
			promptOnFocus = true;
		}
		hasFileChanged(); /* Update the modification stamp */
	}
	
	protected void promptToReplace() {
		promptOnFocus = false;
		
		MessageDialog md = new MessageDialog(getSite().getShell(),
				"Update?", null,
				"Should the contents of this editor be updated?",
				MessageDialog.QUESTION, new String[] { "Yes", "No" }, 1);
		if (md.open() == 0)
			load();
	}
	
	protected abstract ModelObject getModel();
	
	protected void resourceChanged(IResourceDelta delta) {
		if (delta.getResource().equals(getFile()) && !isSaving() && hasFocus()) {
			UI.asyncExec(new Runnable() {
				@Override
				public void run() {
					onChange();
				}
			});
		}
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		if (isSaving())
			return;
		for (IResource r : interestingResources) {
			IResourceDelta specificDelta =
				Project.getSpecificDelta(event.getDelta(), r.getFullPath());
			if (specificDelta != null)
				resourceChanged(specificDelta);
		}
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	abstract protected void doActualSave(IFile f, OutputStream os)
		throws SaveFailedException;
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		setSaving(true);
		IOAdapter io = new IOAdapter();
		FileEditorInput i = (FileEditorInput)getEditorInput();
		try {
			doActualSave(i.getFile(), io.getOutputStream());
			
			Project.setContents(
					i.getFile(), io.getInputStream(),
					new ModificationRunner.Callback() {
				@Override
				public void onSuccess() {
					hasFileChanged(); /* Update the modification stamp */
					firePropertyChange(PROP_DIRTY);
				}
				
				@Override
				public void always() {
					setSaving(false);
				}
			});
		} catch (SaveFailedException e) {
			e.printStackTrace();
			setSaving(false);
			return;
		}
	}
	
	/**
	 * Attempts to return this {@link AbstractEditor}'s input as an {@link
	 * IFile}.
	 * @return an {@link IFile}, if one could be extracted, or {@code null}
	 * otherwise
	 */
	protected IFile getFile() {
		IEditorInput i_ = getEditorInput();
		if (i_ instanceof FileEditorInput) {
			return ((FileEditorInput)i_).getFile();
		} else return null;
	}
	
	/**
	 * Loads the model object into this {@link AbstractEditor}. (Note that this
	 * method may be called more than once!)
	 * @throws LoadFailedException if the loading process fails
	 * @see #loadInput()
	 */
	protected abstract void loadModel() throws LoadFailedException;
	
	/**
	 * Loads this {@link AbstractEditor}'s input {@link IFile} as a {@link
	 * ModelObject}.
	 * @return a {@link ModelObject}, or {@code null} if {@link #getFile()}
	 * returns {@code null} 
	 * @throws LoadFailedException if the file loading process fails
	 */
	protected ModelObject loadInput() throws LoadFailedException {
		IFile file = getFile();
		if (file != null) {
			return new EclipseFileWrapper(file).load();
		} else return null;
	}
	
	private Composite parent;
	
	private final Composite setParent(Composite parent) {
		if (this.parent != null && this.parent != parent)
			throw new RuntimeException("Mysterious parent mismatch");
		this.parent = parent;
		return parent;
	}
	
	protected Composite getParent() {
		return parent;
	}
	
	@Override
	public void doSaveAs() {
		SaveAsDialog d = new SaveAsDialog(getSite().getShell());
		d.setBlockOnOpen(true);
		if (d.open() == Dialog.OK) {
			IFile f = Project.getWorkspaceRoot().getFile(d.getResult());
			setInputWithNotify(new FileEditorInput(f));
			doSave(getEditorSite().getActionBars().
					getStatusLineManager().getProgressMonitor());
		}
	}
	
	private Composite errorContainer, editorContainer;
	private GridData errorContainerData, editorContainerData;
	
	private EditorError editorError;
	
	/**
	 * Creates the error control.
	 * @param parent the parent {@link Composite}, which is guaranteed to use a
	 * {@link FillLayout}
	 */
	protected void createErrorControl(Composite parent) {
		editorError = new EditorError(parent, null);
	}
	
	/**
	 * Updates the error control.
	 * <p>This method should typically cause the error control to display
	 * details of the error presently associated with this {@link
	 * AbstractEditor}.
	 * @see #setError(Exception)
	 */
	protected void updateErrorControl() {
		editorError.setThrowable(getError());
	}
	
	/**
	 * Creates the editor's {@link Control}.
	 * @param parent the parent {@link Composite}, which is guaranteed to use a
	 * {@link FillLayout}
	 */
	protected abstract void createEditorControl(Composite parent);
	
	/**
	 * Updates the editor control.
	 * <p>When this {@link AbstractEditor} has no error associated with it,
	 * this method should typically prepare the model object for editing by
	 * installing it into the control.
	 * @see #setError(Exception)
	 */
	protected abstract void updateEditorControl();
	
	private Exception lastError;
	
	/**
	 * Returns the error presently associated with this {@link AbstractEditor}.
	 * @return an {@link Exception}
	 */
	protected Exception getError() {
		return lastError;
	}
	
	/**
	 * Sets the error presently associated with this {@link AbstractEditor}.
	 * <p>If {@code ex} is {@code null}, then the editor control will be
	 * displayed; if it's non-{@code null}, then the error control will be
	 * displayed instead. (Both the editor control and the error control are
	 * created as needed by this method.)
	 * <p>If the editor or error controls exist, then their respective update
	 * methods will be called before this method returns.
	 * <p>(This method is an important part of the {@link AbstractEditor}
	 * lifecycle.)
	 * @param ex an {@link Exception}
	 */
	protected void setError(Exception ex) {
		lastError = ex;
		boolean error = (ex != null);
		
		if (!error && editorContainer == null) {
			editorContainer = new Composite(getParent(), SWT.NONE);
			editorContainer.setLayoutData(editorContainerData =
					new GridData(SWT.FILL, SWT.FILL, true, true));
			editorContainer.setLayout(new FillLayout());
			createEditorControl(editorContainer);
		} else if (error && errorContainer == null) {
			errorContainer = new Composite(getParent(), SWT.NONE);
			errorContainer.setLayoutData(errorContainerData =
					new GridData(SWT.FILL, SWT.FILL, true, true));
			errorContainer.setLayout(new FillLayout());
			createErrorControl(errorContainer);
		}
		
		if (editorContainer != null) {
			editorContainer.setVisible(!error);
			editorContainerData.exclude = error;
			updateEditorControl();
		}
		if (errorContainer != null) {
			errorContainer.setVisible(error);
			errorContainerData.exclude = !error;
			updateErrorControl();
		}
		
		getParent().layout(true);
	}
	
	@Override
	public final void createPartControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(1, true);
		gl.marginHeight = gl.marginWidth =
				gl.horizontalSpacing = gl.verticalSpacing = 0;
		c.setLayout(gl);
		setParent(c);
		load();
	}
	
	/**
	 * Calls {@link #loadModel()}, followed by:&mdash;
	 * <ul>
	 * <li>{@link #setError(Exception) setError(null)} upon success; or
	 * <li>{@link #setError(Exception)} upon failure.
	 * </ul>
	 */
	protected void load() {
		try {
			loadModel();
			setError(null);
		} catch (LoadFailedException e) {
			setError(e);
		}
	}
	
	private ArrayList<String> globalActionIDs = new ArrayList<String>();
	
	public List<String> getGlobalActionIDs() {
		return globalActionIDs;
	}
	
	/**
	 * Registers the given {@link IAction}s as global action handlers for this
	 * editor.
	 * @param actions a number of {@link IAction}s
	 */
	protected void setGlobalActionHandlers(IAction... actions) {
		IActionBars bars = getEditorSite().getActionBars();
		for (IAction i : actions) {
			String id = i.getId();
			globalActionIDs.add(id);
			bars.setGlobalActionHandler(id, i);
		}
		bars.updateActionBars();
	}
	
	protected void stateChanged() {
		firePropertyChange(PROP_DIRTY);
        updateActions(getStateActions());
	}
	
	private List<IResource> interestingResources = new ArrayList<IResource>();
	
	protected void addInterestingResource(IResource r) {
		if (r != null)
			interestingResources.add(r);
	}
	
	protected void removeInterestingResource(IResource r) {
		if (r != null)
			interestingResources.remove(r);
	}
	
	protected abstract boolean canUndo();
	protected abstract boolean canRedo();
	protected abstract boolean canRevert();
	
	protected abstract void undo();
	protected abstract void redo();
	protected abstract void revert();
	
	@Override
	public boolean canPerformAction(String actionID) {
		if (ActionFactory.UNDO.getId().equals(actionID)) {
			return getError() == null && canUndo();
		} else if (ActionFactory.REDO.getId().equals(actionID)) {
			return getError() == null && canRedo();
		} else if (ActionFactory.REVERT.getId().equals(actionID)) {
			return canRevert();
		} else return false;
	}
	
	@Override
	public void performAction(String actionID) {
		if (ActionFactory.UNDO.getId().equals(actionID)) {
			undo();
		} else if (ActionFactory.REDO.getId().equals(actionID)) {
			redo();
		} else if (ActionFactory.REVERT.getId().equals(actionID)) {
			revert();
		}
	}
}
