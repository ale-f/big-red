package it.uniud.bigredit;

import java.io.OutputStream;
import java.util.ArrayList;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.savers.SaveFailedException;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.tools.ConnectionDragCreationTool;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.editors.assistants.ControlImageDescriptor;
import dk.itu.big_red.editors.bigraph.BigraphEditor;
import dk.itu.big_red.editors.bigraph.BigraphEditorContextMenuProvider;
import dk.itu.big_red.editors.bigraph.ModelFactory;
import dk.itu.big_red.editors.bigraph.NodeFactory;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.actions.FilePrintAction;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;

import it.uniud.bigredit.editparts.PartFactory;
import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;
import it.uniud.bigredit.model.load_save.savers.BRSXMLSaver;

public class PlayEditor extends BigraphEditor {
	public static final String ID = "it.uniud.bigredit.BigreditEditor";
	
	
	//private BRS model = null;
	private BRS model;
	private KeyHandler keyHandler;

	@Override
	public void dispose() {
		getEditDomain().setActiveTool(null);
		super.dispose();
	}
	
	@Override
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
    public void createActions() {
    	registerActions(null, new SelectAllAction(this));
    	
    	/*
    	 * Note to self: actions which are conditionally enabled only when
    	 * certain items are selected must be registered with
    	 * getSelectionActions(), actions which are conditionally enabled when
    	 * the editor state changes must be registered with getStackActions(),
    	 * and I have no idea at all what ActionBarContributors do.
    	 */
    	
    	registerActions(getSelectionActions(),
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
    	setGlobalActionHandlers(registerActions(null,
    			new FilePrintAction(this)));
    }
	
	private static final int INITIAL_SASH_WEIGHTS[] = { 30, 70 };
	
    @Override
	public void createEditorControl(Composite parent) {
		SashForm splitter =
				new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
		
		createPaletteViewer(splitter);
		createGraphicalViewer(splitter);
		splitter.setWeights(INITIAL_SASH_WEIGHTS);
	}
    
    @Override
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

//    	ImageDescriptor id =
//    		Activator.getImageDescriptor("resources/icons/triangle.png");
    	
		for (Control c : signature.getControls())
			palette.add(new CombinedTemplateCreationEntry(c.getName(), "Node",
					Node.class, new NodeFactory(c),new ControlImageDescriptor(c, 16, 16),
					new ControlImageDescriptor(c, 48, 48)));
		
		nodeGroup.setChildren(palette);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class type) {
    	if (type == ZoomManager.class) {
    		return ((ScalableRootEditPart)getGraphicalViewer().getRootEditPart()).getZoomManager();
    	} /*else if (type == IContentOutlinePage.class) {
    		return new BigraphEditorOutlinePage(this);
    	}*/ else if (type == GraphicalViewer.class) {
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
			site = Activator.getImageDescriptor("resources/icons/bigraph-palette/site.png"),
			root = Activator.getImageDescriptor("resources/icons/bigraph-palette/root.png"),
			edge = Activator.getImageDescriptor("resources/icons/bigraph-palette/edge.png");
		
		
		creationGroup.add(new CombinedTemplateCreationEntry("Bigraph", "Add a new bigraph",
				Bigraph.class, new ModelFactory(Bigraph.class), site, site));
		creationGroup.add(new CombinedTemplateCreationEntry("Site", "Add a new site to the bigraph",
				Site.class, new ModelFactory(Site.class), site, site));
		creationGroup.add(new CombinedTemplateCreationEntry("Root", "Add a new root to the bigraph",
				Root.class, new ModelFactory(Root.class), root, root));
		CreationToolEntry drag =
				new CreationToolEntry("Link", "Connect two points with a link",
						new ModelFactory(Edge.class), edge, edge);
			drag.setToolClass(ConnectionDragCreationTool.class);
			creationGroup.add(drag);
		
		ImageDescriptor
			inner = Activator.getImageDescriptor("resources/icons/bigraph-palette/inner.png"),
			outer = Activator.getImageDescriptor("resources/icons/bigraph-palette/outer.png");
		
		creationGroup.add(new CombinedTemplateCreationEntry("Inner name", "Add a new inner name to the bigraph",
				InnerName.class, new ModelFactory(InnerName.class), inner, inner));
		creationGroup.add(new CombinedTemplateCreationEntry("Outer name", "Add a new outer name to the bigraph",
				OuterName.class, new ModelFactory(OuterName.class), outer, outer));
		creationGroup.add(new CombinedTemplateCreationEntry("ReactionRule", "Add a new ReactionRule",
				Bigraph.class, new ModelFactory(Reaction.class), site, site));
		
    	return container;
    }
    
	@Override
	protected PaletteRoot getPaletteRoot() {
		PaletteRoot root = new PaletteRoot();
		nodeGroup = new PaletteGroup("Node...");
		SelectionToolEntry ste = new SelectionToolEntry();
		
		PlayEditor.populatePalette(root, nodeGroup, ste);
		
		root.setDefaultEntry(ste);
		return root;
	}
	
	/*@Override
	public Bigraph getModel() {
		return test;
	}*/
	
	public BRS getBRSModel() {
		return model;
	}
	
	private GraphicalViewer graphicalViewer;
	
	@Override
	protected GraphicalViewer getGraphicalViewer() {
		return graphicalViewer;
	}
	
	@Override
	protected void setGraphicalViewer(GraphicalViewer viewer) {
		getEditDomain().addViewer(viewer);
		graphicalViewer = viewer;
	}
	
	private SelectionSynchronizer selectionSynchronizer;
	
	@Override
	protected SelectionSynchronizer getSelectionSynchronizer() {
		if (selectionSynchronizer == null)
			selectionSynchronizer = new SelectionSynchronizer();
		return selectionSynchronizer;
	}

	@Override
	protected void doActualSave(IFile f, OutputStream os)
			throws SaveFailedException {
		BRSXMLSaver r = new BRSXMLSaver().setModel(getBRSModel());
		r.setFile(new EclipseFileWrapper(f)).
			setOutputStream(os).exportObject();
		
		getCommandStack().markSaveLocation();
		firePropertyChange(IEditorPart.PROP_DIRTY);		
		
	}

	@Override
	protected void loadModel() throws LoadFailedException {
		model = (BRS)loadInput();
		
		if(model == null ){System.out.println("BRS Model is null"); model= new BRS(this);}
		
		if (getBRSModel() == null)
	    	throw new LoadFailedException("BRS Model is null");
	}
	
	@Override
	protected void updateEditorControl() {
		if (getError() != null)
			return;
		PlayEditor.updateNodePalette(nodeGroup, model.getSignature());
	    getGraphicalViewer().setContents(model);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		initializeActionRegistry();
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		org.eclipse.swt.widgets.Control c = getGraphicalViewer().getControl();
		if (c != null && !c.isDisposed())
			c.setFocus();
	}
}
