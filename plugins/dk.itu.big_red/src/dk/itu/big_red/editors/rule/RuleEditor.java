package dk.itu.big_red.editors.rule;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChangeExecutor;
import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.ToggleGridAction;
import org.eclipse.gef.ui.actions.ToggleSnapToGeometryAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.editors.AbstractGEFEditor;
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.BigraphEditor;
import dk.itu.big_red.editors.bigraph.BigraphEditorContextMenuProvider;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.savers.ReactionRuleXMLSaver;
import dk.itu.big_red.utilities.ui.UI;

public class RuleEditor extends AbstractGEFEditor implements
	ISelectionChangedListener, ISelectionProvider {
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == ZoomManager.class) {
			/* required by ZoomComboContributionItem */
			return getScalableRoot(redexViewer).getZoomManager();
		} else return super.getAdapter(adapter);
	}
	
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
		updateActions(getSelectionActions());
		
		if (listeners.size() == 0)
			return;
		SelectionChangedEvent e =
				new SelectionChangedEvent(this, getSelection());
		for (ISelectionChangedListener l : listeners)
			l.selectionChanged(e);
	}
	
	private ScrollingGraphicalViewer redexViewer, reactumViewer;

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
	public void doActualSave(IFile f, OutputStream os)
			throws SaveFailedException {
    	new ReactionRuleXMLSaver().setModel(getModel()).setFile(f).
    		setOutputStream(os).exportObject();
		getCommandStack().markSaveLocation();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		redexViewer = new ScrollingGraphicalViewer();
		reactumViewer = new ScrollingGraphicalViewer();
		firePropertyChange(PROP_INPUT);
	}

	@Override
	public void createPartControl(Composite parent) {
		SashForm splitter = setComposite(new SashForm(setParent(parent),
				SWT.HORIZONTAL | SWT.SMOOTH));
	
		createPaletteViewer(splitter);
		Composite c = new Composite(splitter, SWT.NONE);
		
		splitter.setWeights(BigraphEditor.INITIAL_SASH_WEIGHTS);
		
		GridLayout gl = new GridLayout(3, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		c.setLayout(gl);

		redexViewer.createControl(c);
		redexViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label l = UI.chain(new Label(c, SWT.NONE)).text(
				String.valueOf((char)0x2192)).done();
		l.setFont(UI.tweakFont(l.getFont(), 40, SWT.BOLD));
		l.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
		
		reactumViewer.createControl(c);
		reactumViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		
		configureGraphicalViewer();
		
		org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(
				c, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		gd.heightHint = 100;
		list.setLayoutData(gd);
		
		redexViewer.getControl().setBackground(ColorConstants.listBackground);
		reactumViewer.getControl().setBackground(ColorConstants.listBackground);
	    
		initialise();
	}
	
	protected void configureGraphicalViewer() {
		redexViewer.setEditDomain(getEditDomain());
		reactumViewer.setEditDomain(getEditDomain());
		
		redexViewer.setEditPartFactory(new PartFactory());
		reactumViewer.setEditPartFactory(new PartFactory());
		
		ScalableRootEditPart
			redexRoot = new ScalableRootEditPart(),
			reactumRoot = new ScalableRootEditPart();
		redexViewer.setRootEditPart(redexRoot);
		reactumViewer.setRootEditPart(reactumRoot);
		
		redexViewer.setContextMenu(
			new BigraphEditorContextMenuProvider(
					redexViewer, getActionRegistry()));
		reactumViewer.setContextMenu(
			new BigraphEditorContextMenuProvider(
					reactumViewer, getActionRegistry()));
		
		redexViewer.addSelectionChangedListener(this);
		reactumViewer.addSelectionChangedListener(this);
		getSite().setSelectionProvider(this);
		
		final ZoomManager
			redexZoom = redexRoot.getZoomManager(),
			reactumZoom = reactumRoot.getZoomManager();
		
		redexZoom.setZoomLevels(BigraphEditor.STOCK_ZOOM_LEVELS);
		reactumZoom.setZoomLevels(BigraphEditor.STOCK_ZOOM_LEVELS);
		redexZoom.setZoomLevelContributions(
				BigraphEditor.STOCK_ZOOM_CONTRIBUTIONS);
		reactumZoom.setZoomLevelContributions(
				BigraphEditor.STOCK_ZOOM_CONTRIBUTIONS);
		
	    registerActions(null,
	    		new ZoomInAction(redexZoom), new ZoomOutAction(reactumZoom));
		
		final ZoomListener zoomSynchroniser = new ZoomListener() {
			private boolean lock = false;
			@Override
			public void zoomChanged(double zoom) {
				if (lock)
					return;
				lock = true;
				try {
					redexZoom.setZoom(zoom);
					reactumZoom.setZoom(zoom);
				} finally {
					lock = false;
				}
			}
		};
		
		redexZoom.addZoomListener(zoomSynchroniser);
		reactumZoom.addZoomListener(zoomSynchroniser);
		
		KeyHandler keyHandler = new KeyHandler();
	    keyHandler.put(KeyStroke.getPressed(SWT.DEL, SWT.DEL, 0),
		    	getActionRegistry().getAction(ActionFactory.DELETE.getId()));
		keyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, SWT.NONE),
				new Action() {
			@Override
			public void run() {
				redexZoom.zoomIn();
			}
		});
		keyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT, SWT.NONE),
				new Action() {
			@Override
			public void run() {
				redexZoom.zoomOut();
			}
		});
		
		redexViewer.setKeyHandler(keyHandler);
		reactumViewer.setKeyHandler(keyHandler);
		
		String stateMask = MouseWheelHandler.KeyGenerator.getKey(SWT.CTRL);
	    redexViewer.setProperty(stateMask, MouseWheelZoomHandler.SINGLETON);
	    reactumViewer.setProperty(stateMask, MouseWheelZoomHandler.SINGLETON);
		
	    registerActions(null,
	    		new ToggleGridAction(redexViewer) {
	    			@Override
	    			public void run() {
	    				super.run();
	    				boolean val = isChecked();
	    				reactumViewer.setProperty(
	    						SnapToGrid.PROPERTY_GRID_VISIBLE, val);
	    				reactumViewer.setProperty(
	    						SnapToGrid.PROPERTY_GRID_ENABLED, val);
	    			}
	    		},
	    		new ToggleSnapToGeometryAction(redexViewer) {
	    			@Override
	    			public void run() {
	    				super.run();
	    				boolean val = isChecked();
	    				reactumViewer.setProperty(
	    						SnapToGeometry.PROPERTY_SNAP_ENABLED, val);
	    			}
	    		});
	}
	
	private ReactionRule model;
	
	@Override
	public ReactionRule getModel() {
		return model;
	}
	
	public void setModel(ReactionRule model) {
		this.model = model;
	}
	
	@Override
	protected void initialiseActual() throws Throwable {
		getCommandStack().flush();
		
		setModel((ReactionRule)loadInput());
	    
		if (getModel() == null) {
			replaceWithError(new Exception("Model is null"));
			return;
		}
		
		Signature s = getModel().getRedex().getSignature();
	    updateNodePalette(s);
	    addInterestingResource(ExtendedDataUtilities.getFile(s));
	    redexViewer.setContents(model.getRedex());
	    reactumViewer.setContents(model.getReactum());
    }
	
	private Bigraph getRedex() {
		return (Bigraph)redexViewer.getContents().getModel();
	}
	
	private Bigraph getReactum() {
		return (Bigraph)reactumViewer.getContents().getModel();
	}
	
	private Map<Change, Change> redexToReactum = new HashMap<Change, Change>();
	
	private Change getReactumChange(Change c) {
		Change ch = redexToReactum.get(c);
		if (ch == null) {
			ch = getModel().getReactumChange(c);
			if (ch == Change.INVALID)
				redexToReactum.put(c, ch);
		}
		return ch;
	}
	
	private void processChangeCommand(int detail, ChangeCommand c) {
		IChangeExecutor target = c.getTarget();
		
		if (target == getRedex()) {
			Change reactumChange = getReactumChange(c.getChange());
			
			if (reactumChange == Change.INVALID) {
				/* This is a redex change that doesn't make sense in the
				 * reactum */
				return;
			} else if (detail == CommandStack.POST_UNDO) {
				if (reactumChange.canInvert()) {
					reactumChange = reactumChange.inverse();
				} else throw new Error(
						"BUG: must invert " + reactumChange + ", but can't");
			}
			
			try {
				getReactum().tryApplyChange(reactumChange);
				if (detail != CommandStack.POST_UNDO) {
					redexToReactum.put(c.getChange(), reactumChange);
				} else redexToReactum.remove(c.getChange());
			} catch (ChangeRejectedException cre) {
				throw new Error("BUG: apparently-valid reactum change " + 
						reactumChange + " was rejected");
			}
		} else if (target == getReactum()) {
			Change ch = c.getChange();
			if (detail != CommandStack.POST_UNDO) {
				getModel().getChanges().add(ch);
			} else getModel().getChanges().remove(ch);
		}
	}
	
	@Override
	public void stackChanged(CommandStackEvent event) {
		int detail = event.getDetail() & CommandStack.POST_MASK;
		if (detail != 0) {
			Command c = event.getCommand();
			if (c instanceof CompoundCommand) {
				for (Object i : ((CompoundCommand)c).getCommands())
					if (i instanceof ChangeCommand)
						processChangeCommand(detail, (ChangeCommand)i);
			} else if (c instanceof ChangeCommand) {
				processChangeCommand(detail, (ChangeCommand)c);
			}
		}
		
		super.stackChanged(event);
	}

	@Override
	protected void createActions() {
		registerActions(getSelectionActions(),
			new DeleteAction((IWorkbenchPart)this),
			new ContainerPropertiesAction(this), new ContainerCutAction(this),
			new ContainerCopyAction(this), new BigraphRelayoutAction(this),
			new ContainerPasteAction(this));

		registerActions(null, new SelectAllAction(this));
	}
	
	@Override
	public void setFocus() {
		super.setFocus();
		if (getComposite() == null)
			return;
		getComposite().setFocus();
	}
}