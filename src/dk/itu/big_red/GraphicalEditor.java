package dk.itu.big_red;

import java.util.ArrayList;
import java.util.EventObject;


import org.eclipse.gef.EditPartViewer;
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
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.parts.ScrollableThumbnail;

import dk.itu.big_red.EditorInput;
import dk.itu.big_red.actions.*;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.factories.ThingFactory;
import dk.itu.big_red.part.PartFactory;
import dk.itu.big_red.part.tree.link.LinkTreePartFactory;
import dk.itu.big_red.part.tree.place.PlaceTreePartFactory;
import dk.itu.big_red.tools.ConnectionDragCreationToolEntry;

public class GraphicalEditor extends org.eclipse.gef.ui.parts.GraphicalEditorWithPalette {
	public static final String ID = "big_red.graphicaleditor";
	
	private String displayFilename = null;
	private String filesystemName = null;
	
	/**
	 * Tracks the number of new documents opened.
	 */
	static int untitledCount = 1;
	
	private Bigraph model;
	private KeyHandler keyHandler;
	
	protected class OutlinePage extends ContentOutlinePage {
		/*
		 * This ContentOutlinePage has been tweaked slightly to contain a
		 * second EditPartViewer (one for the place graph, one for the link).
		 */
		private EditPartViewer viewer2;
		private Control control2;
		
		private SashForm sash;
		private TabFolder tabs;
		private ScrollableThumbnail thumbnail;
		private DisposeListener disposeListener;
		
		public OutlinePage() {
			super(new TreeViewer());
			setViewer2(new TreeViewer());
		}
		
		public void createControl(Composite parent) {
			IActionBars bars = getSite().getActionBars();
			ActionRegistry ar = getActionRegistry();
			
			sash = new SashForm(parent, SWT.VERTICAL);
			tabs = new TabFolder(sash, SWT.NONE);
			
			TabItem placeGraphTab = new TabItem(tabs, SWT.NONE);
			placeGraphTab.setText("Place graph");
			placeGraphTab.setControl(getViewer().createControl(tabs));
			
			TabItem linkGraphTab = new TabItem(tabs, SWT.NONE);
			linkGraphTab.setText("Link graph");
			linkGraphTab.setControl(getViewer2().createControl(tabs));
			
			getViewer().setEditDomain(getEditDomain());
			getViewer().setEditPartFactory(new PlaceTreePartFactory());
			getViewer().setContents(model);
			
			getViewer2().setEditDomain(getEditDomain());
			getViewer2().setEditPartFactory(new LinkTreePartFactory());
			getViewer2().setContents(model);
			
			getSelectionSynchronizer().addViewer(getViewer());
			getSelectionSynchronizer().addViewer(getViewer2());
			
			Canvas canvas = new Canvas(sash, SWT.BORDER);
			LightweightSystem lws = new LightweightSystem(canvas);
			
			thumbnail = new ScrollableThumbnail((Viewport)((ScalableRootEditPart)getGraphicalViewer().getRootEditPart()).getFigure());
			thumbnail.setSource(((ScalableRootEditPart)getGraphicalViewer().getRootEditPart()).getLayer(LayerConstants.PRINTABLE_LAYERS));
			lws.setContents(thumbnail);
			
			disposeListener = new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					if (thumbnail != null) {
						thumbnail.deactivate();
						thumbnail = null;
					}
				}
			};
			
			bars.setGlobalActionHandler(ActionFactory.COPY.getId(), ar.getAction(ActionFactory.COPY.getId()));
			bars.setGlobalActionHandler(ActionFactory.PASTE.getId(), ar.getAction(ActionFactory.PASTE.getId()));
			
			getGraphicalViewer().getControl().addDisposeListener(disposeListener);
		}
		
		public void init(IPageSite pageSite) {
			super.init(pageSite);
			
			getViewer().setContextMenu(
				new AppContextMenuProvider(getViewer(), getActionRegistry()));
			
			IActionBars bars = getSite().getActionBars();
			
			bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), getActionRegistry().getAction(ActionFactory.UNDO.getId()));
			bars.setGlobalActionHandler(ActionFactory.REDO.getId(), getActionRegistry().getAction(ActionFactory.REDO.getId()));
			bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), getActionRegistry().getAction(ActionFactory.DELETE.getId()));
			bars.updateActionBars();
			
			getViewer().setKeyHandler(keyHandler);
		}
		
		public Control getControl() {
			return sash;
		}
		
		public void dispose() {
			getSelectionSynchronizer().removeViewer(getViewer());
			getSelectionSynchronizer().removeViewer(getViewer2());
			
			if (getGraphicalViewer().getControl() != null &&
				!getGraphicalViewer().getControl().isDisposed()) {
				getGraphicalViewer().getControl().removeDisposeListener(disposeListener);
			}
			
	        Control c = getControl2();
	        if (c != null && !c.isDisposed())
				c.dispose();
	        
			super.dispose();
		}

		public void setViewer2(EditPartViewer viewer2) {
			this.viewer2 = viewer2;
		}

		public EditPartViewer getViewer2() {
			return viewer2;
		}

		public void setControl2(Control control2) {
			this.control2 = control2;
		}

		public Control getControl2() {
			return control2;
		}
		
		public void createControl2(Composite parent) {
			control2 = getViewer2().createControl(parent);
		}
	}
	
	public GraphicalEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public String getDisplayFilename() {
		String inputName = getEditorInput().getName();
		if (!inputName.equals("#empty")) {
			return inputName;
		} else {
			if (this.displayFilename == null) {
				this.displayFilename = "Untitled bigraph " + untitledCount;
				untitledCount++;
			}
			return this.displayFilename;
		}
	}
	
	public String getAssociatedFile() {
		return this.filesystemName;
	}

	public void setAssociatedFile(String filesystemName) {
		this.filesystemName = filesystemName;
		setInput(new EditorInput(filesystemName));
		setPartName(getDisplayFilename());
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
	    	new AppContextMenuProvider(viewer, getActionRegistry()));
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
    	
    	action = new ThingRelayoutAction(this);
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
    	
    	action = new FileSaveAction(this);
    	registry.registerAction(action);
    	getEditorSite().getActionBars().
    		setGlobalActionHandler(ActionFactory.SAVE.getId(), action);
    	
    	action = new FileSaveAsAction(this);
    	registry.registerAction(action);
    	getEditorSite().getActionBars().
    		setGlobalActionHandler(ActionFactory.SAVE_AS.getId(), action);
    	
    	action = new FileRevertAction(this);
    	registry.registerAction(action);
    	getEditorSite().getActionBars().
    		setGlobalActionHandler(ActionFactory.REVERT.getId(), action);
    }
    
    protected void initializeGraphicalViewer() {
	    GraphicalViewer viewer = getGraphicalViewer();
	    String inputName = getEditorInput().getName();
	    if (!inputName.equals("#empty")) {
	    	model = Bigraph.fromXML(inputName);
	    	setAssociatedFile(inputName);
	    } else {
	    	model = new Bigraph();
	    	Signature signature = model.getSignature();
	    	
	    	dk.itu.big_red.model.Control b = signature.addControl("Building", "B", Shape.SHAPE_OVAL,
	    			             new Point(250, 250), true),
	    	r = signature.addControl("Room", "R", Shape.SHAPE_OVAL,
	    			             new Point(125, 200), true),
	    	a = signature.addControl("Agent", "A", Shape.SHAPE_TRIANGLE,
	    			             new Point(25, 50), false),
	    	c = signature.addControl("Computer", "C", Shape.SHAPE_RECTANGLE,
	    			             new Point(25, 13), false);
	    	
	    	b.addPort("lan", 2);
	    	
	    	c.addPort("keyboard", 0);
	    	c.addPort("ethernet", 2);
	    	
	    	a.addPort("conference", 0);
	    	a.addPort("hands", 2);
	    	
	    	Root r0 = new Root();
	    	model.addChild(r0);
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
	    }
	    
	    viewer.setContents(model);
	    viewer.addDropTargetListener(new AppTemplateTransferDropTargetListener(viewer));
	    setPartName(getDisplayFilename());
    }
    
    protected void initializePaletteViewer() {
    	super.initializePaletteViewer();
    	// XXX (FIXME?) - what's going on here? Why not an AppTemplate (etc.)?
    	// (tutorial page 72)
    	getPaletteViewer().addDragSourceListener(
    		new TemplateTransferDragSourceListener(getPaletteViewer()));
    }
    
    @SuppressWarnings("unchecked")
	public Object getAdapter(Class type) {
    	if (type == ZoomManager.class) {
    		return ((ScalableRootEditPart)getGraphicalViewer().getRootEditPart()).getZoomManager();
    	} else if (type == IContentOutlinePage.class) {
    		return new OutlinePage();
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
				Node.class, new ThingFactory(Node.class), null, null));
		creationGroup.add(new CombinedTemplateCreationEntry("Site", "Add a new site to the bigraph",
				Site.class, new ThingFactory(Site.class), null, null));
		creationGroup.add(new CombinedTemplateCreationEntry("Root", "Add a new root to the bigraph",
				Root.class, new ThingFactory(Root.class), null, null));
		creationGroup.add(new ConnectionDragCreationToolEntry("Edge", "Connect two nodes with a new edge",
				new ThingFactory(Edge.class), null, null));
		
		creationGroup.add(new CombinedTemplateCreationEntry("Name", "Add a new name to the bigraph",
				Name.class, new ThingFactory(Name.class), null, null));
		
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
		return true;
	}
	
	@Override
	public void doSaveAs() {
		IAction action = new FileSaveAsAction(this);
		action.run();
	}
	
	@Override
	public void doSave(IProgressMonitor monitor) {
		IAction action = new FileSaveAction(this);
		action.run();
		getCommandStack().markSaveLocation();
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
}
