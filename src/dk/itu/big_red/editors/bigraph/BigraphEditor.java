package dk.itu.big_red.editors.bigraph;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SaveAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.AbstractEditor;
import dk.itu.big_red.editors.AbstractGEFEditor;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.actions.FilePrintAction;
import dk.itu.big_red.editors.bigraph.actions.FileRevertAction;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.assistants.NodeFactory;
import dk.itu.big_red.model.import_export.BigraphXMLExport;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;

public class BigraphEditor extends AbstractGEFEditor
implements IResourceChangeListener, CommandStackListener, ISelectionListener {
	public static final String ID = "dk.itu.big_red.BigraphEditor";
	
	private Bigraph model;
	private KeyHandler keyHandler;
	
	private List<String> selectionActions = new ArrayList<String>();
	private List<String> propertyActions = new ArrayList<String>();

	@Override
	public void dispose() {
		getCommandStack().removeCommandStackListener(this);
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(this);
		getEditDomain().setActiveTool(null);
		
		super.dispose();
	}
	
	protected void configureGraphicalViewer() {
    	getGraphicalViewer().getControl().setBackground(
				ColorConstants.listBackground);
    	
	    GraphicalViewer viewer = getGraphicalViewer();
	    viewer.setEditPartFactory(new PartFactory());
	    
	    ScalableRootEditPart rootEditPart = new ScalableRootEditPart();
	    viewer.setRootEditPart(rootEditPart);
	    
	    double[] zoomLevels = new double[] {
	    	 0.25, 0.5, 0.75, 1.0, 1.5, 2.0,
	    	 2.5, 3.0, 4.0, 5.0, 10.0, 20.0 
	    };
	    ZoomManager manager = rootEditPart.getZoomManager();
	    getActionRegistry().registerAction(new ZoomInAction(manager));
	    getActionRegistry().registerAction(new ZoomOutAction(manager));
	    manager.setZoomLevels(zoomLevels);
	     
	    ArrayList<String> zoomContributions = new ArrayList<String>();
	    zoomContributions.add(ZoomManager.FIT_ALL);
	    zoomContributions.add(ZoomManager.FIT_HEIGHT);
	    zoomContributions.add(ZoomManager.FIT_WIDTH);
	    manager.setZoomLevelContributions(zoomContributions);
	     
	    keyHandler = new KeyHandler();
	    keyHandler.put(KeyStroke.getPressed(SWT.DEL, SWT.DEL, 0),
	    	getActionRegistry().getAction(ActionFactory.DELETE.getId()));
	    keyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
	    	getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
	    keyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT, 0),
	    	getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));
	    viewer.setKeyHandler(keyHandler);
	    
	    viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.CTRL),
	    	MouseWheelZoomHandler.SINGLETON);
	     
	    viewer.setContextMenu(
	    	new BigraphEditorContextMenuProvider(viewer, getActionRegistry()));
	}
    
	@Override
	protected void firePropertyChange(int propertyId) {
		super.firePropertyChange(propertyId);
		updateActions(propertyActions);
	}
	
	@Override
    public void createActions() {
    	AbstractEditor.registerActions(getActionRegistry(), getStateActions(),
    		new UndoAction(this), new RedoAction(this));
    	
    	AbstractEditor.registerActions(getActionRegistry(), null,
    		new SelectAllAction(this));
    	
    	AbstractEditor.registerActions(getActionRegistry(), propertyActions,
    		new SaveAction(this));
    	
    	/*
    	 * Note to self: actions which are conditionally enabled only when
    	 * certain items are selected must be registered with
    	 * getSelectionActions(), actions which are conditionally enabled when
    	 * the editor state changes must be registered with getStackActions(),
    	 * and I have no idea at all what ActionBarContributors do.
    	 */
    	
    	AbstractEditor.registerActions(getActionRegistry(), selectionActions,
    		new ContainerPropertiesAction(this), new ContainerCutAction(this),
    		new ContainerCopyAction(this), new ContainerPasteAction(this),
    		new BigraphRelayoutAction(this),
    		new DeleteAction((IWorkbenchPart)this));
    	
    	/*
    	 * Does this kind of action need to be registered in the
    	 * ActionRegistry? What does the ActionRegistry *do*, anyway? (Are most
    	 * Eclipse projects comprised primarily of comments saying "What does
    	 * the <insert name here> *do*, anyway?"?)
    	 */
    	IAction action = new FilePrintAction(this);
    	getActionRegistry().registerAction(action);
    	getEditorSite().getActionBars().
    		setGlobalActionHandler(ActionFactory.PRINT.getId(), action);
    	
    	action = new FileRevertAction(this);
    	getActionRegistry().registerAction(action);
    	getEditorSite().getActionBars().
    		setGlobalActionHandler(ActionFactory.REVERT.getId(), action);    	
    	getStateActions().add(ActionFactory.REVERT.getId());
    }
    
    protected void createPaletteViewer(Composite parent) {
		PaletteViewer viewer = new PaletteViewer();
		setPaletteViewer(viewer);
		viewer.createControl(parent);
		getEditDomain().setPaletteViewer(getPaletteViewer());
	}
	
	private PaletteViewer paletteViewer;
	
	protected void setPaletteViewer(PaletteViewer paletteViewer) {
		this.paletteViewer = paletteViewer;
	}
    
	protected PaletteViewer getPaletteViewer() {
		return paletteViewer;
	}
	
	private static final int INITIAL_SASH_WEIGHTS[] = { 30, 70 };
	
    @Override
	public void createPartControl(Composite parent) {
		SashForm splitter = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
		setComposite(splitter);
		
		createPaletteViewer(splitter);
		createGraphicalViewer(splitter);
		splitter.setWeights(INITIAL_SASH_WEIGHTS);
		
		try {
			initialiseActual();
		} catch (Throwable t) {
			replaceWithError(t);
		}
	}
    
    protected void createGraphicalViewer(Composite parent) {
		GraphicalViewer viewer = new ScrollingGraphicalViewer();
		viewer.createControl(parent);
		setGraphicalViewer(viewer);
		configureGraphicalViewer();
		
		getSelectionSynchronizer().addViewer(getGraphicalViewer());
		getSite().setSelectionProvider(getGraphicalViewer());
	}
    
	public static void updateNodePalette(PaletteContainer nodeGroup, Signature signature) {
    	ArrayList<PaletteEntry> palette = new ArrayList<PaletteEntry>();

    	ImageDescriptor id =
    		RedPlugin.getImageDescriptor("resources/icons/triangle.png");
    	
		for (Control c : signature.getControls())
			palette.add(new CombinedTemplateCreationEntry(c.getName(), "Node",
					Node.class, new NodeFactory(c), id, id));
		
		nodeGroup.setChildren(palette);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class type) {
    	if (type == ZoomManager.class) {
    		return ((ScalableRootEditPart)getGraphicalViewer().getRootEditPart()).getZoomManager();
    	} else if (type == IPropertySheetPage.class) {
    		PropertySheetPage psp = new PropertySheetPage();
    		psp.setRootEntry(new ChangePropertySheetEntry(getCommandStack()));
    		return psp;
    	} else if (type == IContentOutlinePage.class) {
    		return new BigraphEditorOutlinePage(this);
    	} else if (type == GraphicalViewer.class) {
			return getGraphicalViewer();
    	} else if (type == EditPart.class && getGraphicalViewer() != null) {
			return getGraphicalViewer().getRootEditPart();
    	} else if (type == IFigure.class && getGraphicalViewer() != null) {
			return ((GraphicalEditPart) getGraphicalViewer().getRootEditPart())
					.getFigure();
		} else return super.getAdapter(type);
    }
    
	
	@Override
	/* Provisionally */ public DefaultEditDomain getEditDomain() {
		return super.getEditDomain();
	}
	
    private PaletteGroup nodeGroup;
    
    public static <T extends PaletteContainer> T populatePalette(T container, PaletteGroup nodeGroup, SelectionToolEntry defaultTool) {
    	PaletteGroup selectGroup = new PaletteGroup("Object selection");
		selectGroup.setId("BigraphEditor.palette.selection");
		container.add(selectGroup);
		
		selectGroup.add((defaultTool != null ? defaultTool : new SelectionToolEntry()));
		selectGroup.add(new MarqueeToolEntry());
		
		container.add(new PaletteSeparator());
		
		PaletteGroup creationGroup = new PaletteGroup("Object creation");
		creationGroup.setId("BigraphEditor.palette.creation");
		container.add(creationGroup);
		
		if (nodeGroup == null)
			nodeGroup = new PaletteGroup("Node...");
		nodeGroup.setId("BigraphEditor.palette.node-creation");
		creationGroup.add(nodeGroup);

		ImageDescriptor
			site = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/site.png"),
			root = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/root.png"),
			edge = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/edge.png");
		
		creationGroup.add(new CombinedTemplateCreationEntry("Site", "Add a new site to the bigraph",
				Site.class, new ModelFactory(Site.class), site, site));
		creationGroup.add(new CombinedTemplateCreationEntry("Root", "Add a new root to the bigraph",
				Root.class, new ModelFactory(Root.class), root, root));
		creationGroup.add(new ConnectionDragCreationToolEntry("Edge", "Connect two nodes with a new edge",
				new ModelFactory(Edge.class), edge, edge));
		
		ImageDescriptor
			inner = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/inner.png"),
			outer = RedPlugin.getImageDescriptor("resources/icons/bigraph-palette/outer.png");
		
		creationGroup.add(new CombinedTemplateCreationEntry("Inner name", "Add a new inner name to the bigraph",
				InnerName.class, new ModelFactory(InnerName.class), inner, inner));
		creationGroup.add(new CombinedTemplateCreationEntry("Outer name", "Add a new outer name to the bigraph",
				OuterName.class, new ModelFactory(OuterName.class), outer, outer));
		
    	return container;
    }
    
	@Override
	protected PaletteRoot getPaletteRoot() {
		PaletteRoot root = new PaletteRoot();
		nodeGroup = new PaletteGroup("Node...");
		SelectionToolEntry ste = new SelectionToolEntry();
		
		BigraphEditor.populatePalette(root, nodeGroup, ste);
		
		root.setDefaultEntry(ste);
		return root;
	}
	
	@Override
    public void commandStackChanged(EventObject event) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
        updateActions(getStateActions());
    }
	
	public void revert() {
		CommandStack cs = getCommandStack();
		while (isDirty())
			cs.undo();
		cs.flush();
	}
	
	@Override
	public Bigraph getModel() {
		return model;
	}
	
	protected GraphicalViewer graphicalViewer;
	
	protected GraphicalViewer getGraphicalViewer() {
		return graphicalViewer;
	}
	
	protected void setGraphicalViewer(GraphicalViewer viewer) {
		getEditDomain().addViewer(viewer);
		graphicalViewer = viewer;
	}
	
	private SelectionSynchronizer selectionSynchronizer;
	
	protected SelectionSynchronizer getSelectionSynchronizer() {
		if (selectionSynchronizer == null)
			selectionSynchronizer = new SelectionSynchronizer();
		return selectionSynchronizer;
	}
	
	private boolean hasFocus() {
		return UI.getWorkbenchPage().getActiveEditor().equals(this);
	}
	
	private boolean changeNotificationWaiting = false;
	
	private void doChangeDialog() {
		UI.askFor(getSite().getShell(),
			"File changed",
			"The file '" + getModel().getFile() + "' has been changed on " +
			"the file system. Do you want to replace the editor contents " +
			"with these changes?", UI.YES_NO);
	}
	
	@Override
	public void setFocus() {
		getGraphicalViewer().getControl().setFocus();
		
		if (changeNotificationWaiting) {
			changeNotificationWaiting = false;
			doChangeDialog();
		}
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta specificDelta =
			Project.getSpecificDelta(event.getDelta(), getModel().getFile());
		if (specificDelta != null && !isSaving()) {
			if (hasFocus()) {
				doChangeDialog();
			} else changeNotificationWaiting = true;
		}
	}

	@Override
	protected void doActualSave(OutputStream os) throws ExportFailedException {
		new BigraphXMLExport().setModel(getModel()).setOutputStream(os).
			exportObject();
		
		getCommandStack().markSaveLocation();
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}

	@Override
	protected void initialiseActual() throws Throwable {
		IEditorInput input = getEditorInput();
	    setPartName(input.getName());
	    
	    if (input instanceof FileEditorInput) {
	    	FileEditorInput fi = (FileEditorInput)input;
	    	try {
	    		model = (Bigraph)Import.fromFile(fi.getFile());
	    	} catch (Exception e) {
	    		throw e;
	    	}
	    }
	    
	    if (model == null) {
	    	model = new Bigraph();
	    } else updateNodePalette(nodeGroup, model.getSignature());
	    
	    getGraphicalViewer().setContents(model);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		getCommandStack().addCommandStackListener(this);
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
		initializeActionRegistry();
	}

	@Override
	protected void initializeActionRegistry() {
		super.initializeActionRegistry();
		updateActions(propertyActions);
		updateActions(getStateActions());
	}
	
	@Override
	public boolean isDirty() {
		return getCommandStack().isDirty();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (equals(getSite().getPage().getActiveEditor()))
			updateActions(selectionActions);
	}
}
