package dk.itu.big_red.editors.rule;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SaveAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.bigraph.BigraphEditorContextMenuProvider;
import dk.itu.big_red.editors.bigraph.actions.BigraphCheckpointAction;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.import_export.ReactionRuleXMLExport;
import dk.itu.big_red.model.import_export.ReactionRuleXMLImport;
import dk.itu.big_red.util.FileResourceOutputStream;
import dk.itu.big_red.util.UI;
import dk.itu.big_red.util.ValidationFailedException;

public class RuleEditor extends EditorPart implements
	CommandStackListener, ISelectionListener, INullSelectionListener,
	ISelectionChangedListener, ISelectionProvider {
	private DefaultEditDomain editDomain = new DefaultEditDomain(this);

	private ArrayList<ISelectionChangedListener> listeners =
		new ArrayList<ISelectionChangedListener>();
	
	private ISelection selection = null;
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return selection;
	}

	@Override
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		this.selection = selection;
	}
	
	private ActionRegistry actionRegistry = new ActionRegistry();
	private List<String> selectionActions = new ArrayList<String>();
	private List<String> stackActions = new ArrayList<String>();
	private List<String> propertyActions = new ArrayList<String>();
	
	private ScrollingGraphicalViewer redexViewer, reactumViewer;
	
	/**
	 * Fired by the workbench when some kind of global overarching selection
	 * changes.
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
	}

	/**
	 * Fired by the redex and reactum viewers when their selections change.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		setSelection(event.getSelection());
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	ReactionRuleXMLExport ex = new ReactionRuleXMLExport();
        	
        	ex.setModel(getModel()).setOutputStream(new FileResourceOutputStream(i.getFile())).exportObject();
        	
    		getCommandStack().markSaveLocation();
    		firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (Exception ex) {
        	if (monitor != null)
        		monitor.setCanceled(true);
        	ErrorDialog.openError(getSite().getShell(), null, "Unable to save the document.",
	    		RedPlugin.getThrowableStatus(ex));
        }		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		redexViewer = new ScrollingGraphicalViewer();
		reactumViewer = new ScrollingGraphicalViewer();
		
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
		return getCommandStack().isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout gl = new GridLayout(3, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		c.setLayout(gl);
		
		redexViewer.createControl(c);
		redexViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label l = new Label(c, SWT.NONE);
		l.setFont(UI.tweakFont(l.getFont(), 40, SWT.BOLD));
		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		l.setText("→");
		
		reactumViewer.createControl(c);
		reactumViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		org.eclipse.swt.widgets.List list =
			new org.eclipse.swt.widgets.List(c, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		gd.heightHint = 100;
		list.setLayoutData(gd);
		
		redexViewer.getControl().setBackground(ColorConstants.listBackground);
		reactumViewer.getControl().setBackground(ColorConstants.listBackground);
		
		redexViewer.setEditDomain(editDomain);
		reactumViewer.setEditDomain(editDomain);
		
		redexViewer.setEditPartFactory(new PartFactory());
		reactumViewer.setEditPartFactory(new PartFactory());
		
		redexViewer.setRootEditPart(new ScalableRootEditPart());
		reactumViewer.setRootEditPart(new ScalableRootEditPart());

		createActions();
		
		redexViewer.setContextMenu(
			new BigraphEditorContextMenuProvider(redexViewer, getActionRegistry()));
		reactumViewer.setContextMenu(
			new BigraphEditorContextMenuProvider(reactumViewer, getActionRegistry()));
		
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
		redexViewer.addSelectionChangedListener(this);
		reactumViewer.addSelectionChangedListener(this);
		getSite().setSelectionProvider(this);
	
		getCommandStack().addCommandStackListener(this);
		
		loadInput();
	}

	private ReactionRule model;
	
	public ReactionRule getModel() {
		return model;
	}
	
	public void setModel(ReactionRule model) {
		this.model = model;
	}
	
	protected void loadInput() {
		IEditorInput input = getEditorInput();
	    if (input instanceof FileEditorInput) {
	    	FileEditorInput fi = (FileEditorInput)input;
	    	try {
	    		setModel(ReactionRuleXMLImport.importFile(fi.getFile()));
	    	} catch (ImportFailedException e) {
	    		e.printStackTrace();
	    		Throwable cause = e.getCause();
	    		if (cause instanceof ValidationFailedException) {
	    			return;
	    		} else {
	    			return;
	    		}
	    	} catch (Exception e) {
	    		return;
	    	}
	    }
	    
	    if (getModel() == null)
	    	setModel(new ReactionRule());
	    
	    redexViewer.setContents(model.getRedex());
	    setPartName(getEditorInput().getName());
    }
	
	/**
	 * Returns the command stack.
	 * @return the command stack
	 */
	protected CommandStack getCommandStack() {
		return getEditDomain().getCommandStack();
	}
	
	/**
	 * Returns the edit domain.
	 * @return the edit domain
	 */
	protected DefaultEditDomain getEditDomain() {
		return editDomain;
	}

	@Override
    public void commandStackChanged(EventObject event) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
        updateActions(stackActions);
    }
	
	public static void registerActions(ActionRegistry registry,
			List<String> actionIDList, IAction... actions) {
		for (IAction i : actions) {
			registry.registerAction(i);
			if (actionIDList != null)
				actionIDList.add(i.getId());
		}
	}
	
	/**
	 * Creates actions for this editor. Subclasses should override this method
	 * to create and register actions with the {@link ActionRegistry}.
	 */
	protected void createActions() {
		ActionRegistry registry = getActionRegistry();
		
		registerActions(registry, stackActions,
			new UndoAction(this), new RedoAction(this));
		
		registerActions(registry, selectionActions,
			new DeleteAction((IWorkbenchPart)this),
			new ContainerPropertiesAction(this), new ContainerCutAction(this),
			new ContainerCopyAction(this), new BigraphRelayoutAction(this),
			new BigraphCheckpointAction(this));

		registerActions(registry, null,
			new SelectAllAction(this));
		
		registerActions(registry, propertyActions,
			new SaveAction(this));
	}

	/**
	 * Initializes the ActionRegistry. This registry may be used by
	 * {@link ActionBarContributor ActionBarContributors} and/or
	 * {@link ContextMenuProvider ContextMenuProviders}.
	 * <P>
	 * This method may be called on Editor creation, or lazily the first time
	 * {@link #getActionRegistry()} is called.
	 */
	protected void initializeActionRegistry() {
		createActions();
		updateActions(propertyActions);
		updateActions(stackActions);
	}
	
	protected ActionRegistry getActionRegistry() {
		return actionRegistry;
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * A convenience method for updating a set of actions defined by the given
	 * List of action IDs. The actions are found by looking up the ID in the
	 * {@link #getActionRegistry() action registry}. If the corresponding action
	 * is an {@link UpdateAction}, it will have its <code>update()</code> method
	 * called.
	 * 
	 * @param actionIds
	 *            the list of IDs to update
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
		if (adapter == CommandStack.class) {
			return getCommandStack();
		} else return super.getAdapter(adapter);
	}
}