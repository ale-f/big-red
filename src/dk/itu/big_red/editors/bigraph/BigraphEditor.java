package dk.itu.big_red.editors.bigraph;

import java.util.ArrayList;
import java.util.EventObject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.LayerManager;
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
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.actions.FilePrintAction;
import dk.itu.big_red.editors.bigraph.actions.FileRevertAction;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.import_export.ImportFailedException;
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
import dk.itu.big_red.model.import_export.BigraphXMLImport;
import dk.itu.big_red.utilities.ValidationFailedException;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;

public class BigraphEditor extends org.eclipse.gef.ui.parts.GraphicalEditorWithPalette {
	public static final String ID = "dk.itu.big_red.BigraphEditor";
	
	private Bigraph model = null;
	private KeyHandler keyHandler;
	
	public BigraphEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}
	
    @Override
	protected void configureGraphicalViewer() {
	    super.configureGraphicalViewer();   
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
    
    @SuppressWarnings("unchecked")
	@Override
    public void createActions() {
    	super.createActions();
    	
    	/*
    	 * Note to self: actions which are conditionally enabled only when
    	 * certain items are selected must be registered with
    	 * getSelectionActions(), actions which are conditionally enabled when
    	 * the editor state changes must be registered with getStackActions(),
    	 * and I have no idea at all what ActionBarContributors do.
    	 */
    	
    	ActionRegistry registry = getActionRegistry();
    	IAction action = new ContainerPropertiesAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());
    	
    	action = new ContainerCutAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());
    	
    	action = new ContainerCopyAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());
    	
    	action = new ContainerPasteAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());
    	
    	action = new BigraphRelayoutAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());
    	
    	/*
    	 * Does this kind of action need to be registered in the
    	 * ActionRegistry? What does the ActionRegistry *do*, anyway? (Are most
    	 * Eclipse projects comprised primarily of comments saying "What does
    	 * the <insert name here> *do*, anyway?"?)
    	 */
    	action = new FilePrintAction(this);
    	registry.registerAction(action);
    	getEditorSite().getActionBars().
    		setGlobalActionHandler(ActionFactory.PRINT.getId(), action);
    	
    	action = new FileRevertAction(this);
    	registry.registerAction(action);
    	getEditorSite().getActionBars().
    		setGlobalActionHandler(ActionFactory.REVERT.getId(), action);    	
    	
    	getStackActions().add(ActionFactory.REVERT.getId());
    }
    
    @Override
	protected void initializeGraphicalViewer() {
	    IEditorInput input = getEditorInput();
	    setPartName(input.getName());
	    
	    if (input instanceof FileEditorInput) {
	    	FileEditorInput fi = (FileEditorInput)input;
	    	try {
	    		model = BigraphXMLImport.importFile(fi.getFile());
	    	} catch (ImportFailedException e) {
	    		e.printStackTrace();
	    		Throwable cause = e.getCause();
	    		if (cause instanceof ValidationFailedException) {
	    			UI.openError("Validation has failed.", cause);
	    		} else {
	    			UI.openError("Opening the document failed.", e);
	    		}
	    	} catch (Exception e) {
	    		UI.openError("An unexpected error occurred.", e);
	    	}
	    }
	    
	    if (model == null) {
	    	model = new Bigraph();
	    } else updateNodePalette(nodeGroup, model.getSignature());
	    
	    getGraphicalViewer().setContents(model);
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
    	} else return super.getAdapter(type);
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
		/*
		 * Why on earth is this necessary?
		 */
        firePropertyChange(IEditorPart.PROP_DIRTY);
        super.commandStackChanged(event);
    }
	
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}
	
	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			IOAdapter io = new IOAdapter();
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	BigraphXMLExport ex = new BigraphXMLExport();
        	
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
	
	public void revert() {
		CommandStack cs = getCommandStack();
		while (isDirty())
			cs.undo();
		cs.flush();
	}
	
	public Bigraph getModel() {
		return (Bigraph) getGraphicalViewer().getContents().getModel();
	}
	
	@Override
	public GraphicalViewer getGraphicalViewer() {
		return super.getGraphicalViewer();
	}
	
	public IFigure getPrintLayer() {
		return ((LayerManager)getGraphicalViewer().getEditPartRegistry().get(LayerManager.ID)).
			getLayer(LayerConstants.PRINTABLE_LAYERS);
	}
	
	@Override
	public SelectionSynchronizer getSelectionSynchronizer() {
		return super.getSelectionSynchronizer();
	}
	
	@Override
	public DefaultEditDomain getEditDomain() {
		return super.getEditDomain();
	}
}
