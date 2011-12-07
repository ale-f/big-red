package dk.itu.big_red.editors.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
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
import dk.itu.big_red.editors.bigraph.ConnectionDragCreationToolEntry;
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
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.import_export.ReactionRuleXMLExport;
import dk.itu.big_red.model.import_export.ReactionRuleXMLImport;
import dk.itu.big_red.util.UI;
import dk.itu.big_red.util.ValidationFailedException;
import dk.itu.big_red.util.resources.FileResourceOutputStream;

public class RuleEditor extends EditorPart implements
	ISelectionListener, INullSelectionListener, ISelectionChangedListener,
	ISelectionProvider, CommandStackEventListener {
	private DefaultEditDomain editDomain = new DefaultEditDomain(this);

	private ArrayList<ISelectionChangedListener> listeners =
		new ArrayList<ISelectionChangedListener>();
	
	private ISelection selection = null;
	
	/**
	 * A map from entities in the redex to entities in the reactum.
	 */
	private HashMap<ModelObject, ModelObject> reactumEntities =
		new HashMap<ModelObject, ModelObject>();
	
	@SuppressWarnings("unchecked")
	private <T> T getReactumEntity(T redexEntity) {
		return (T)reactumEntities.get(redexEntity);
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

	private Composite parent, self;
	
	private void error(Throwable t) {
		self.dispose(); self = null;
		Label l = new Label(parent, SWT.CENTER);
		l.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		l.setText(t.toString());
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
		
		getCommandStack().addCommandStackEventListener(this);
		
		loadInput();
	}
	
	private PaletteGroup nodeGroup;
	
	private PaletteRoot getPaletteRoot() {
		PaletteRoot root = new PaletteRoot();
		
		PaletteToolbar tb = new PaletteToolbar("Horizontal palette");
		root.add(tb);
		
		PaletteGroup selectGroup = new PaletteGroup("Object selection");
		selectGroup.setId("BigraphEditor.palette.selection");
		tb.add(selectGroup);
		
		selectGroup.add(new SelectionToolEntry());
		selectGroup.add(new MarqueeToolEntry());
		
		tb.add(new PaletteSeparator());
		
		PaletteGroup creationGroup = new PaletteGroup("Object creation");
		creationGroup.setId("BigraphEditor.palette.creation");
		tb.add(creationGroup);
		
		nodeGroup = new PaletteGroup("Node...");
		nodeGroup.setId("BigraphEditor.palette.node-creation");
		creationGroup.add(nodeGroup);
				
		ImageDescriptor imd = ImageDescriptor.getMissingImageDescriptor();
		
		creationGroup.add(new CombinedTemplateCreationEntry("Site", "Add a new site to the bigraph",
				Site.class, new ModelFactory(Site.class), imd, imd));
		creationGroup.add(new CombinedTemplateCreationEntry("Root", "Add a new root to the bigraph",
				Root.class, new ModelFactory(Root.class), imd, imd));
		creationGroup.add(new ConnectionDragCreationToolEntry("Edge", "Connect two nodes with a new edge",
				new ModelFactory(Edge.class), imd, imd));
		
		creationGroup.add(new CombinedTemplateCreationEntry("Inner name", "Add a new inner name to the bigraph",
				InnerName.class, new ModelFactory(InnerName.class), imd, imd));
		creationGroup.add(new CombinedTemplateCreationEntry("Outer name", "Add a new outer name to the bigraph",
				OuterName.class, new ModelFactory(OuterName.class), imd, imd));
		
		root.setDefaultEntry((ToolEntry) selectGroup.getChildren().get(0));
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
	    
	    redexViewer.setContents(model.getRedex());
	    reactumViewer.setContents(model.getRedex().clone(reactumEntities));
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
	
	private Change createReactumChange(Change redexChange, ChangeGroup cg) {
		Change copy = null;
		if (redexChange instanceof ChangeGroup) {
			ChangeGroup cg_ = (ChangeGroup)redexChange;
			for (Change i : cg_)
				createReactumChange(i, cg);
		} else if (redexChange instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild ch = ac(redexChange);
			
			Container reactumParent = getReactumEntity(ch.parent);
			Layoutable reactumChild = getReactumEntity(ch.child);
			
			if (reactumParent == null)
				System.exit(-1);
			if (reactumChild == null)
				reactumChild = ch.child.clone(reactumEntities);
			
			copy = new Container.ChangeAddChild(reactumParent, reactumChild, ch.name);
		} else if (redexChange instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout ch = ac(redexChange);
			
			Layoutable reactumModel = getReactumEntity(ch.model);
			
			if (reactumModel == null)
				System.exit(-1);
			
			copy =
				new Layoutable.ChangeLayout(reactumModel, ch.newLayout.getCopy());
		} else if (redexChange instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild ch = ac(redexChange);
			
			Container reactumParent = getReactumEntity(ch.parent);
			Layoutable reactumChild = getReactumEntity(ch.child);
			
			if (reactumParent == null || reactumChild == null)
				System.exit(-1);
			
			copy =
				new Container.ChangeRemoveChild(reactumParent, reactumChild);
			reactumEntities.remove(ch.child);
		}
		if (copy != null)
			cg.add(copy);
		return cg;
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
			getReactum().applyChange(
				createReactumChange(ch, new ChangeGroup()));
			
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
		} else return super.getAdapter(adapter);
	}
}