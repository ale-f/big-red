package dk.itu.big_red.editors.bigraph;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.savers.BigraphXMLSaver;
import org.bigraph.model.savers.SaveFailedException;
import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import dk.itu.big_red.editors.AbstractGEFEditor;
import dk.itu.big_red.editors.actions.TogglePropertyAction;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.actions.FilePrintAction;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;

public class BigraphEditor extends AbstractGEFEditor {
	public static final String ID = "dk.itu.big_red.BigraphEditor";
	
	private Bigraph model;
	private KeyHandler keyHandler;

	@Override
	public void dispose() {
		getEditDomain().setActiveTool(null);
		super.dispose();
	}
	
	public static final List<String> STOCK_ZOOM_CONTRIBUTIONS =
			new ArrayList<String>();
	static {
		STOCK_ZOOM_CONTRIBUTIONS.add(ZoomManager.FIT_ALL);
		STOCK_ZOOM_CONTRIBUTIONS.add(ZoomManager.FIT_HEIGHT);
		STOCK_ZOOM_CONTRIBUTIONS.add(ZoomManager.FIT_WIDTH);
	}
	
	public static final double[] STOCK_ZOOM_LEVELS = new double[] {
		0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 10.0, 20.0 
	};
	
	protected void configureGraphicalViewer() {
    	getGraphicalViewer().getControl().setBackground(
				ColorConstants.listBackground);
    	
	    GraphicalViewer viewer = getGraphicalViewer();
	    viewer.setEditPartFactory(new PartFactory());
	    
	    ScalableRootEditPart rootEditPart = new ScalableRootEditPart();
	    viewer.setRootEditPart(rootEditPart);
	    
	    ZoomManager manager = rootEditPart.getZoomManager();
	    registerActions(null,
	    		new ZoomInAction(manager), new ZoomOutAction(manager));
	    manager.setZoomLevels(STOCK_ZOOM_LEVELS);
	    manager.setZoomLevelContributions(STOCK_ZOOM_CONTRIBUTIONS);
	     
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
	    
	    registerActions(null,
	    		new ToggleGridAction(getGraphicalViewer()),
	    		new ToggleSnapToGeometryAction(getGraphicalViewer()),
	    		new TogglePropertyAction(
	    				PROPERTY_DISPLAY_GUIDES, true, getGraphicalViewer()),
	    		new TogglePropertyAction(
	    				PROPERTY_DISPLAY_EDGES, true, getGraphicalViewer()));
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
	
	public static final int INITIAL_SASH_WEIGHTS[] = { 20, 80 };
	
    @Override
	public void createEditorControl(Composite parent) {
		SashForm splitter = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
		
		createPaletteViewer(splitter);
		createGraphicalViewer(splitter);
		splitter.setWeights(INITIAL_SASH_WEIGHTS);
	}
    
    protected void createGraphicalViewer(Composite parent) {
		GraphicalViewer viewer = new ScrollingGraphicalViewer();
		viewer.createControl(parent);
		setGraphicalViewer(viewer);
		configureGraphicalViewer();
		
		getSelectionSynchronizer().addViewer(getGraphicalViewer());
		getSite().setSelectionProvider(getGraphicalViewer());
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class type) {
    	if (type == ZoomManager.class) {
    		ScalableRootEditPart sep = getScalableRoot(getGraphicalViewer());
    		return (sep != null ? sep.getZoomManager() : null);
    	} else if (type == IContentOutlinePage.class && getModel() != null) {
    		return new BigraphEditorOutlinePage(this);
    	} else if (type == GraphicalViewer.class) {
			return getGraphicalViewer();
    	} else if (type == EditPart.class && getGraphicalViewer() != null) {
			return getGraphicalViewer().getRootEditPart();
    	} else if (type == IFigure.class && getGraphicalViewer() != null) {
    		ScalableRootEditPart sep = getScalableRoot(getGraphicalViewer());
			return (sep != null ? sep.getFigure() : null);
		} else return super.getAdapter(type);
    }
    
	@Override
	/* Provisionally */ public DefaultEditDomain getEditDomain() {
		return super.getEditDomain();
	}
	
	@Override
	public Bigraph getModel() {
		return model;
	}
	
	private GraphicalViewer graphicalViewer;
	
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

	@Override
	protected void doActualSave(IFile f, OutputStream os)
			throws SaveFailedException {
		BigraphXMLSaver r = new BigraphXMLSaver().setModel(getModel());
		r.setFile(new EclipseFileWrapper(f)).
			setOutputStream(os).exportObject();
		getCommandStack().markSaveLocation();
	}

	@Override
	protected void loadModel() throws LoadFailedException {
		model = (Bigraph)loadInput();
	}
	
	@Override
	protected void updateEditorControl() {
		if (getError() != null)
			return;
		getCommandStack().flush();
		updateNodePalette(model.getSignature());
		getGraphicalViewer().setContents(model);
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		GraphicalViewer gv = getGraphicalViewer();
		if (gv != null) {
			Control c = gv.getControl();
			if (c != null && !c.isDisposed())
				c.setFocus();
		}
	}
}
