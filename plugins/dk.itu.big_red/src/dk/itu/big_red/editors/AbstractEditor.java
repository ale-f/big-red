package dk.itu.big_red.editors;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.assistants.EditorError;
import dk.itu.big_red.editors.assistants.RedoProxyAction;
import dk.itu.big_red.editors.assistants.RedoProxyAction.IRedoImplementor;
import dk.itu.big_red.editors.assistants.UndoProxyAction;
import dk.itu.big_red.editors.assistants.UndoProxyAction.IUndoImplementor;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.load_save.Loader;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;

public abstract class AbstractEditor extends EditorPart
implements IResourceChangeListener, IUndoImplementor, IRedoImplementor {
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
		
		if (actionRegistry != null)
			getActionRegistry().dispose();
		
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
	 * <p>Subclasses should override this method, but they should also call
	 * <code>super.initializeActionRegistry()</code> before doing anything
	 * else.
	 */
	protected void initializeActionRegistry() {
		setGlobalActionHandlers(registerActions(getStateActions(),
				new UndoProxyAction(this), new RedoProxyAction(this)));
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
		super.setInput(input);
		setPartName(input.getName());
	}
	
	@Override
	protected void setInputWithNotify(IEditorInput input) {
		setInput(input);
        firePropertyChange(PROP_INPUT);
	}
	
	protected abstract ModelObject getModel();
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta specificDelta =
			Project.getSpecificDelta(event.getDelta(),
					((FileEditorInput)getEditorInput()).getFile());
		if (specificDelta != null && !isSaving())
			;
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
		try {
			IOAdapter io = new IOAdapter();
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	
        	doActualSave(i.getFile(), io.getOutputStream());
        	
        	Project.setContents(i.getFile(), io.getInputStream());
    		firePropertyChange(PROP_DIRTY);
		} catch (SaveFailedException cre) {
			cre.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			setSaving(false);
		}
	}
	
	protected ModelObject loadInput() throws Throwable {
		IEditorInput i_ = getEditorInput();
		if (i_ instanceof FileEditorInput) {
			return Loader.fromFile(((FileEditorInput)i_).getFile());
		} else return null;
	}
	
	/**
	 * Initialises the editor once the controls have been created.
	 * <p>Anything thrown by this method will cause the editor to be torn down
	 * and replaced with an {@link EditorError}.
	 * @throws Throwable if something went wrong
	 */
	abstract protected void initialiseActual() throws Throwable;
	
	protected void initialise() {
		try {
			initialiseActual();
		} catch (Throwable t) {
			replaceWithError(t);
		}
	}
	
	private Composite self;
	
	protected <T extends Composite> T setComposite(T self) {
		this.self = self;
		return self;
	}
	
	protected Composite getComposite() {
		return self;
	}
	
	private Composite parent;
	
	protected Composite setParent(Composite parent) {
		if (this.parent != null && this.parent != parent)
			throw new Error("Mysterious parent mismatch");
		this.parent = parent;
		return parent;
	}
	
	@Override
	public void doSaveAs() {
		SaveAsDialog d = new SaveAsDialog(getSite().getShell());
		d.setBlockOnOpen(true);
		if (d.open() == Dialog.OK) {
			IFile f = Project.getWorkspaceRoot().getFile(d.getResult());
			setInputWithNotify(new FileEditorInput(f));
			doSave(null);
		}
	}
	
	protected void replaceWithError(Throwable t) {
		Composite parent = getComposite().getParent();
		getComposite().dispose(); setComposite(null);
		new EditorError(parent, RedPlugin.getThrowableStatus(t));
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
		getEditorSite().getActionBars().updateActionBars();
	}
	
	protected void stateChanged() {
		firePropertyChange(PROP_DIRTY);
        updateActions(getStateActions());
	}
}
