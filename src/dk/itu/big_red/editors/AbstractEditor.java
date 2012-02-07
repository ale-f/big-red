package dk.itu.big_red.editors;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.IFileBackable;
import dk.itu.big_red.utilities.resources.Project;

public abstract class AbstractEditor extends EditorPart
implements IResourceChangeListener {
	public AbstractEditor() {
		Project.getWorkspace().
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
		Project.getWorkspace().removeResourceChangeListener(this);
		getModel().dispose();
		
		if (actionRegistry != null)
			getActionRegistry().dispose();
		
		super.dispose();
	}
	
	/**
	 * Registers a number of {@link IAction}s with the given {@link
	 * ActionRegistry}, optionally copying their IDs into a {@link List}.
	 * @param registry an {@link ActionRegistry}
	 * @param actionIDList a list to be filled with {@link String} IDs; can be
	 * <code>null</code>
	 * @param actions a number of {@link IAction}s
	 */
	public static void registerActions(ActionRegistry registry,
		List<String> actionIDList, IAction... actions) {
		for (IAction i : actions) {
			registry.registerAction(i);
			if (actionIDList != null)
				actionIDList.add(i.getId());
		}
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
	
	protected IFile getFile() {
		return (getModel() instanceof IFileBackable ?
				((IFileBackable)getModel()).getFile() :
					(getEditorInput() instanceof FileEditorInput ?
						((FileEditorInput)getEditorInput()).getFile() : null));
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta specificDelta =
				Project.getSpecificDelta(event.getDelta(), getFile());
		if (specificDelta != null && !isSaving())
			;
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	abstract protected void doActualSave(OutputStream os)
		throws ExportFailedException;
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		setSaving(true);
		try {
			IOAdapter io = new IOAdapter();
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	
        	doActualSave(io.getOutputStream());
        	
        	Project.setContents(i.getFile(), io.getInputStream());
    		firePropertyChange(IEditorPart.PROP_DIRTY);
		} catch (ExportFailedException cre) {
			cre.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			setSaving(false);
		}
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
	
	protected void setComposite(Composite self) {
		this.self = self;
	}
	
	protected Composite getComposite() {
		return self;
	}
	
	@Override
	public void doSaveAs() {
		SaveAsDialog d = new SaveAsDialog(getSite().getShell());
		d.setBlockOnOpen(true);
		if (d.open() == Dialog.OK) {
			IFile f = Project.getWorkspaceFile(d.getResult());
			if (getModel() instanceof IFileBackable)
				((IFileBackable)getModel()).setFile(f);
			
			setInputWithNotify(new FileEditorInput(f));
			doSave(null);
		}
	}
	
	protected void replaceWithError(Throwable t) {
		Composite parent = getComposite().getParent();
		getComposite().dispose(); setComposite(null);
		new EditorError(parent, RedPlugin.getThrowableStatus(t));
	}
}
