package dk.itu.big_red.editors.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SaveAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IAction;
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
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.bigraph.BigraphEditor;
import dk.itu.big_red.editors.bigraph.BigraphEditorContextMenuProvider;
import dk.itu.big_red.editors.bigraph.ChangePropertySheetEntry;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.import_export.ReactionRuleXMLExport;
import dk.itu.big_red.model.import_export.ReactionRuleXMLImport;
import dk.itu.big_red.utilities.ValidationFailedException;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.EditorError;
import dk.itu.big_red.utilities.ui.UI;

public class RuleEditor extends EditorPart implements
	ISelectionListener, INullSelectionListener, ISelectionChangedListener,
	ISelectionProvider, CommandStackEventListener {
	private DefaultEditDomain editDomain = new DefaultEditDomain(this);

	private ArrayList<ISelectionChangedListener> listeners =
		new ArrayList<ISelectionChangedListener>();
	
	private ISelection selection = null;
	
	@SuppressWarnings("unchecked")
	private <T> T getReactumEntity(T redexEntity) {
		return (T)model.getRedexToReactumMap().get(redexEntity);
	}
	
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
		updateActions(selectionActions);
		
		if (listeners.size() == 0)
			return;
		SelectionChangedEvent e =
				new SelectionChangedEvent(this, getSelection());
		for (ISelectionChangedListener l : listeners)
			l.selectionChanged(e);
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

	private boolean ignoringSelectionUpdates = false;
	
	/**
	 * Fired by the redex and reactum viewers when their selections change.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (ignoringSelectionUpdates)
			return;
		ignoringSelectionUpdates = true;
		
		if (!event.getSelection().isEmpty())
			(event.getSource() == reactumViewer ?
					redexViewer : reactumViewer).deselectAll();
		
		setSelection(event.getSelection());
		ignoringSelectionUpdates = false;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			IOAdapter io = new IOAdapter();
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	ReactionRuleXMLExport ex = new ReactionRuleXMLExport();
        	
        	ex.setModel(getModel()).setOutputStream(io.getOutputStream()).exportObject();
        	Project.setContents(i.getFile(), io.getInputStream());
        	
    		getCommandStack().markSaveLocation();
    		firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (Exception ex) {
        	if (monitor != null)
        		monitor.setCanceled(true);
        	UI.openError("Unable to save the document.", ex);
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

	private Composite parent, self;
	
	private void error(Throwable t) {
		self.dispose(); self = null;
		new EditorError(parent, RedPlugin.getThrowableStatus(t));
	}
	
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		self = new Composite(parent, SWT.NONE);
		
		Composite c = self;
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout gl = new GridLayout(3, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		c.setLayout(gl);
		
		PaletteViewer pv = new PaletteViewer();
		pv.createControl(c);
		pv.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		pv.setPaletteRoot(getPaletteRoot());
		
		getEditDomain().setPaletteViewer(pv);
		
		redexViewer.createControl(c);
		redexViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label l = UI.newLabel(c, SWT.NONE, "→");
		l.setFont(UI.tweakFont(l.getFont(), 40, SWT.BOLD));
		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		
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
		
		getCommandStack().addCommandStackEventListener(this);
		
		loadInput();
	}
	
	private PaletteGroup nodeGroup;
	
	private PaletteRoot getPaletteRoot() {
		PaletteRoot root = new PaletteRoot();
		
		PaletteToolbar tb = new PaletteToolbar("Horizontal palette");
		root.add(tb);
		
		SelectionToolEntry ste = new SelectionToolEntry();
		nodeGroup = new PaletteGroup("Node...");
		
		BigraphEditor.populatePalette(tb, nodeGroup, ste);
		
		root.setDefaultEntry(ste);
		return root;
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
		setPartName(input.getName());
		
	    if (input instanceof FileEditorInput) {
	    	FileEditorInput fi = (FileEditorInput)input;
	    	try {
	    		setModel(ReactionRuleXMLImport.importFile(fi.getFile()));
	    	} catch (ImportFailedException e) {
	    		e.printStackTrace();
	    		Throwable cause = e.getCause();
	    		if (cause instanceof ValidationFailedException) {
	    			error(e);
	    			return;
	    		} else {
	    			error(e);
	    			return;
	    		}
	    	} catch (Exception e) {
	    		error(e);
	    		return;
	    	}
	    }
	    
	    BigraphEditor.updateNodePalette(nodeGroup,
	    	model.getRedex().getSignature());
	    
	    redexViewer.setContents(model.getRedex());
	    reactumViewer.setContents(model.getReactum());
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
	
	@SuppressWarnings("unchecked")
	private static <V, T> V ac(T x) {
		return (V)x;
	}
	
	private Change createReactumChange(Change redexChange) {
		Change reactumChange = null;
		if (redexChange instanceof ChangeGroup) {
			ChangeGroup cg_ = (ChangeGroup)redexChange,
				cg = new ChangeGroup();
			for (Change i : cg_)
				cg.add(createReactumChange(i));
			
			reactumChange = cg;
		} else if (redexChange instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild ch = ac(redexChange);
			
			Container reactumParent = getReactumEntity(ch.getCreator());
			Layoutable reactumChild = getReactumEntity(ch.child);
			
			if (reactumParent == null)
				return null;
			if (reactumChild == null)
				reactumChild = ch.child.clone(model.getRedexToReactumMap());
			
			/*
			 * XXX: a BigraphScratchpad should really be used here so that
			 * ChangeGroups will actually work properly
			 */
			String reactumName;
			Map<String, Layoutable> reactumNamespace =
				getReactum().getNamespace(Bigraph.getNSI(reactumChild));
			if (reactumNamespace.get(ch.name) == null) {
				reactumName = ch.name;
			} else reactumName = Bigraph.getFirstUnusedName(reactumNamespace);
			
			reactumChange =
				reactumParent.changeAddChild(reactumChild, reactumName);
		} else if (redexChange instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout ch = ac(redexChange);
			
			Layoutable reactumModel = getReactumEntity(ch.getCreator());
			
			if (reactumModel == null)
				return null;
			
			reactumChange =
				reactumModel.changeLayout(ch.newLayout.getCopy());
		} else if (redexChange instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild ch = ac(redexChange);
			
			Container reactumParent = getReactumEntity(ch.getCreator());
			Layoutable reactumChild = getReactumEntity(ch.child);
			
			if (reactumParent == null || reactumChild == null)
				return null;
			
			reactumChange =
				reactumParent.changeRemoveChild(reactumChild);
			model.getRedexToReactumMap().remove(ch.child);
		} else if (redexChange instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName ch = ac(redexChange);
			
			Layoutable reactumModel = getReactumEntity(ch.getCreator());
			if (reactumModel == null)
				return null;
			
			reactumChange = reactumModel.changeName(ch.newName);
		} else if (redexChange instanceof Point.ChangeConnect) {
			Point.ChangeConnect ch = ac(redexChange);
			
			Point reactumPoint = getReactumEntity(ch.getCreator());
			Link reactumLink = getReactumEntity(ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			reactumChange = reactumPoint.changeConnect(reactumLink);
		} else if (redexChange instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect ch = ac(redexChange);
			
			Point reactumPoint = getReactumEntity(ch.getCreator());
			Link reactumLink = getReactumEntity(ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			reactumChange = reactumPoint.changeDisconnect(reactumLink);
		} else if (redexChange instanceof Site.ChangeAlias) {
			Site.ChangeAlias ch = ac(redexChange);
			
			Site reactumSite = getReactumEntity(ch.getCreator());
			if (reactumSite == null)
				return null;
			
			reactumChange = reactumSite.changeAlias(ch.alias);
		}
		System.out.println(redexChange + "\n\t->" + reactumChange);
		return reactumChange;
	}
	
	private Bigraph getRedex() {
		return (Bigraph)redexViewer.getContents().getModel();
	}
	
	private Bigraph getReactum() {
		return (Bigraph)reactumViewer.getContents().getModel();
	}
	
	private void processChangeCommand(int detail, ChangeCommand c) {
		IChangeable target = c.getTarget();
		
		Change ch;
		if (detail != CommandStack.POST_UNDO) {
			ch = c.getChange();
		} else ch = c.getChange().inverse();
		
		if (target == getRedex()) {
			System.out.println("Event (" + detail + ") from redex: " + c.getChange());
			Change reactumChange = createReactumChange(ch);
			try {
				getReactum().tryApplyChange(reactumChange);
			} catch (ChangeRejectedException cre) {
				cre.killVM();
			}
			
			if (getModel().getChanges().size() > 0) {
				try {
					getReactum().tryApplyChange(getModel().getChanges());
				} catch (ChangeRejectedException cre) {
					cre.killVM();
				}
			}
		} else if (target == getReactum()) {
			System.out.println("Event (" + detail + ") from reactum: " + c.getChange());
			getModel().getChanges().add(ch);
		}
	}
	
	@Override
	public void stackChanged(CommandStackEvent event) {
		int detail = event.getDetail() & CommandStack.POST_MASK;
		if (detail != 0) {
			Command c = event.getCommand();
			if (c instanceof CompoundCommand) {
				@SuppressWarnings("unchecked")
				List<Command> cmds = ((CompoundCommand)c).getCommands();
				for (Command i : cmds)
					if (i instanceof ChangeCommand)
						processChangeCommand(detail, (ChangeCommand)i);
			} else if (c instanceof ChangeCommand) {
				processChangeCommand(detail, (ChangeCommand)c);
			}
		}
		
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
			new ContainerPasteAction(this));

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
		} else if (adapter == IPropertySheetPage.class) {
			PropertySheetPage psp = new PropertySheetPage();
			psp.setRootEntry(new ChangePropertySheetEntry(getCommandStack()));
			return psp;
		} else if (adapter == CommandStack.class) {
			return getCommandStack();
		} else if (adapter == ActionRegistry.class) {
			return getActionRegistry();
		} else return super.getAdapter(adapter);
	}
}