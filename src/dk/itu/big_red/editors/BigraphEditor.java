package dk.itu.big_red.editors;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.EventObject;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.SaveAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.actions.*;
import dk.itu.big_red.editors.assistants.BigraphEditorTemplateTransferDropTargetListener;
import dk.itu.big_red.editors.assistants.BigraphEditorContextMenuProvider;
import dk.itu.big_red.editors.assistants.BigraphEditorOutlinePage;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.assistants.ResourceWrapper;
import dk.itu.big_red.model.import_export.BigraphXMLExport;
import dk.itu.big_red.model.import_export.BigraphXMLImport;
import dk.itu.big_red.part.PartFactory;
import dk.itu.big_red.tools.ConnectionDragCreationToolEntry;

public class BigraphEditor extends org.eclipse.gef.ui.parts.GraphicalEditorWithPalette {
	public static final String ID = "dk.itu.big_red.BigraphEditor";
	
	private ResourceWrapper<Bigraph> model = new ResourceWrapper<Bigraph>();
	private KeyHandler keyHandler;
	
	public BigraphEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}
	
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
    	
    	ActionRegistry registry = getActionRegistry();
    	IAction action = new ThingPropertiesAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());
    	
    	action = new ThingCutAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());
    	
    	action = new ThingCopyAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());
    	
    	action = new ThingPasteAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());
    	
    	/*action = new ThingRelayoutAction(this);
    	registry.registerAction(action);
    	getSelectionActions().add(action.getId());*/
    	
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
    	
    	action = new SaveAction(this);
    	registry.registerAction(action);
    	getEditorSite().getActionBars().
    		setGlobalActionHandler(ActionFactory.SAVE.getId(), action);
    	
    	action = new FileRevertAction(this);
    	registry.registerAction(action);
    	getEditorSite().getActionBars().
    		setGlobalActionHandler(ActionFactory.REVERT.getId(), action);
    }
    
    protected void initializeGraphicalViewer() {
	    GraphicalViewer viewer = getGraphicalViewer();
	    IEditorInput input = getEditorInput();
	    if (input instanceof FileEditorInput) {
	    	FileEditorInput fi = (FileEditorInput)input;
	    	model.setResource(fi.getFile());
	    	try {
	    		BigraphXMLImport im = new BigraphXMLImport();
	    		im.setInputStream(fi.getFile().getContents());
	    		
	    		model.setModel(im.importModel());
	    	} catch (Exception e) {
	    		e.printStackTrace();
		    	model.setModel(new Bigraph());
		    	Signature signature = model.getModel().getSignature();
		    	
		    	dk.itu.big_red.model.Control b =
		    		signature.addControl(new Control("Building", "B", Shape.SHAPE_OVAL, null,
		    				new Point(250, 250), true)),
		    	r = signature.addControl(new Control("Room", "R", Shape.SHAPE_OVAL, null,
		    			new Point(125, 200), true)),
		    	a = signature.addControl(new Control("Agent", "A", Shape.SHAPE_POLYGON,
		    			dk.itu.big_red.model.Control.POINTS_TRIANGLE,
		    			new Point(25, 50), false)),
		    	c = signature.addControl(new Control("Computer", "C", Shape.SHAPE_POLYGON,
		    			dk.itu.big_red.model.Control.POINTS_QUAD, new Point(25, 13), false));
		    	
		    	b.addPort("a", 0, 0.33);
		    	
		    	c.addPort("b", 0, 0.45);
		    	
		    	a.addPort("c", 0, 0.66);
		    	
		    	r.addPort("d", 0, 0.78);
		    	
		    	Root r0 = new Root();
		    	model.getModel().addChild(r0);
		    	r0.setLayout(new Rectangle(10, 10, 400, 400));
		    		Node building0 = new Node(b);
		    		r0.addChild(building0);
		    		building0.setLayout(new Rectangle(10, 10, 250, 250));
		    			Node room0 = new Node(r);
		    			building0.addChild(room0);
		    			room0.setLayout(new Rectangle(10, 10, 125, 200));
		    				Node agent0 = new Node(a);
		    				room0.addChild(agent0);
		    				agent0.setLayout(new Rectangle(10, 10, 0, 0));
		    		Node building1 = new Node(b);
		    		r0.addChild(building1);
		    		building1.setLayout(new Rectangle(20, 20, 100, 100));
		    			Node room1 = new Node(r);
		    			building1.addChild(room1);
		    			room1.setLayout(new Rectangle(10, 10, 50, 50));
	    	}
	    }
	    
	    viewer.setContents(model.getModel());
	    viewer.addDropTargetListener(new BigraphEditorTemplateTransferDropTargetListener(viewer));
	    setPartName(getEditorInput().getName());
    }
    
    protected void initializePaletteViewer() {
    	super.initializePaletteViewer();
    	// XXX (FIXME?) - what's going on here? Why not an AppTemplate (etc.)?
    	// (tutorial page 72)
    	getPaletteViewer().addDragSourceListener(
    		new TemplateTransferDragSourceListener(getPaletteViewer()));
    }
    
    @SuppressWarnings("rawtypes")
	public Object getAdapter(Class type) {
    	if (type == ZoomManager.class) {
    		return ((ScalableRootEditPart)getGraphicalViewer().getRootEditPart()).getZoomManager();
    	} else if (type == IContentOutlinePage.class) {
    		return new BigraphEditorOutlinePage(this);
    	} else return super.getAdapter(type);
    }
    
	@Override
	protected PaletteRoot getPaletteRoot() {
		// TODO Auto-generated method stub
		PaletteRoot root = new PaletteRoot();
		
		PaletteGroup selectGroup = new PaletteGroup("Object selection");
		root.add(selectGroup);
		
		selectGroup.add(new SelectionToolEntry());
		selectGroup.add(new MarqueeToolEntry());
		
		root.add(new PaletteSeparator());
		
		PaletteGroup creationGroup = new PaletteGroup("Object creation");
		root.add(creationGroup);
		
		creationGroup.add(new CombinedTemplateCreationEntry("Node", "Add a new node to the bigraph",
				Node.class, new ModelFactory(Node.class), null, null));
		creationGroup.add(new CombinedTemplateCreationEntry("Site", "Add a new site to the bigraph",
				Site.class, new ModelFactory(Site.class), null, null));
		creationGroup.add(new CombinedTemplateCreationEntry("Root", "Add a new root to the bigraph",
				Root.class, new ModelFactory(Root.class), null, null));
		creationGroup.add(new ConnectionDragCreationToolEntry("Edge", "Connect two nodes with a new edge",
				new ModelFactory(Edge.class), null, null));
		
		creationGroup.add(new CombinedTemplateCreationEntry("Name", "Add a new name to the bigraph",
				InnerName.class, new ModelFactory(InnerName.class), null, null));
		
		root.setDefaultEntry((ToolEntry) selectGroup.getChildren().get(0));
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
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	ByteArrayOutputStream os = new ByteArrayOutputStream();
        	BigraphXMLExport ex = new BigraphXMLExport();
        	
        	ex.setModel(getModel());
        	ex.setOutputStream(os);
        	ex.exportModel();
        	
    		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    		i.getFile().setContents(is, 0, null);
        	
    		getCommandStack().markSaveLocation();
    		firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (Exception ex) {
        	if (monitor != null)
        		monitor.setCanceled(true);
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
