package dk.itu.big_red.editors.rule;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.savers.ReactionRuleXMLSaver;
import org.bigraph.model.savers.SaveFailedException;
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
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.editors.AbstractGEFEditor;
import dk.itu.big_red.editors.actions.TogglePropertyAction;
import dk.itu.big_red.editors.bigraph.BigraphEditor;
import dk.itu.big_red.editors.bigraph.BigraphEditorContextMenuProvider;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
import dk.itu.big_red.utilities.ui.UI;

public class RuleEditor extends AbstractGEFEditor implements
		ISelectionChangedListener, ISelectionProvider {
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == ZoomManager.class) {
			/* required by ZoomComboContributionItem */
			ScalableRootEditPart sep = getScalableRoot(redexViewer);
			return (sep != null ? sep.getZoomManager() : null);
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
    	ReactionRuleXMLSaver r = new ReactionRuleXMLSaver().setModel(getModel());
		r.setFile(new EclipseFileWrapper(f)).
    		setOutputStream(os).exportObject();
		getCommandStack().markSaveLocation();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
	}

	@Override
	public void createEditorControl(Composite parent) {
		SashForm splitter = new SashForm(parent, SWT.HORIZONTAL | SWT.SMOOTH);
		
		redexViewer = new ScrollingGraphicalViewer();
		reactumViewer = new ScrollingGraphicalViewer();
		
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
		
		Label l = new Label(c, SWT.NONE);
		l.setText("\u2192");
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
	}
	
	protected void configureGraphicalViewer() {
		redexViewer.setEditDomain(getEditDomain());
		reactumViewer.setEditDomain(getEditDomain());
		
		redexViewer.setEditPartFactory(new PartFactory());
		reactumViewer.setEditPartFactory(new PartFactory() {
			@Override
			public IPropertySource getPropertySource(Object o) {
				return (o instanceof Layoutable ?
						new ReactumPropertySource((Layoutable)o) : null);
			}
		});
		
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
	    				reactumViewer.setProperty(
	    						SnapToGrid.PROPERTY_GRID_VISIBLE, isChecked());
	    				reactumViewer.setProperty(
	    						SnapToGrid.PROPERTY_GRID_ENABLED, isChecked());
	    			}
	    		},
	    		new ToggleSnapToGeometryAction(redexViewer) {
	    			@Override
	    			public void run() {
	    				super.run();
	    				reactumViewer.setProperty(
	    						SnapToGeometry.PROPERTY_SNAP_ENABLED,
	    						isChecked());
	    			}
	    		},
	    		new TogglePropertyAction(
	    				PROPERTY_DISPLAY_GUIDES, true, redexViewer) {
	    			@Override
	    			public void run() {
	    				super.run();
	    				reactumViewer.setProperty(
	    						PROPERTY_DISPLAY_GUIDES, isChecked());
	    			}
	    		},
	    		new TogglePropertyAction(
	    				PROPERTY_DISPLAY_EDGES, true, redexViewer) {
	    			@Override
	    			public void run() {
	    				super.run();
	    				reactumViewer.setProperty(
	    						PROPERTY_DISPLAY_EDGES, isChecked());
	    			}
	    		});
	}
	
	private ReactionRule model;
	
	@Override
	public ReactionRule getModel() {
		return model;
	}
	
	@Override
	protected void loadModel() throws LoadFailedException {
		model = (ReactionRule)loadInput();
		try {
	    	reactum = model.createReactum();
	    } catch (ChangeCreationException e) {
	    	throw new LoadFailedException(e);
	    }
	}
	
	@Override
	protected void updateEditorControl() {
		if (getError() != null)
			return;
		getCommandStack().flush();
	    updateNodePalette(getModel().getRedex().getSignature());
	    redexViewer.setContents(model.getRedex());
	    reactumViewer.setContents(getReactum());
    }
	
	private Bigraph getRedex() {
		return getModel().getRedex();
	}
	
	private Bigraph reactum;
	
	private Bigraph getReactum() {
		return reactum;
	}
	
	@Override
	public void stackChanged(CommandStackEvent event) {
		int detail = event.getDetail() & CommandStack.PRE_MASK;
		if (detail != 0)
			_testConvert(event.getDetail(), event.getCommand());
		super.stackChanged(event);
	}

	private void _testConvert(int detail, Command c) {
		if (c instanceof ChangeCommand) {
			_testConvertChange(detail, (ChangeCommand)c);
		} else if (c instanceof CompoundCommand) {
			for (Object i : ((CompoundCommand)c).getCommands())
				if (i instanceof Command)
					_testConvert(detail, (Command)i);
		}
	}
	
	private Map<IChangeDescriptor, IChangeDescriptor>
		safeRedexToReactum =
				new HashMap<IChangeDescriptor, IChangeDescriptor>();
	
	private void _testConvertChange(int detail, ChangeCommand c) {
		IChangeDescriptor commandChange = c.getChange();
		Object target = c.getContext();
		
		ChangeDescriptorGroup reactumChanges = getModel().getEdit().getDescriptors();
		if (target == getRedex()) {
			IChangeDescriptor cd =
				(detail != CommandStack.PRE_UNDO ?
					commandChange : commandChange.inverse());
			
			ChangeDescriptorGroup lRedexCDs =
					DescriptorUtilities.linearise(cd);
			ChangeDescriptorGroup cdg =
					ReactionRule.performFixups(lRedexCDs, reactumChanges);

			/* Integrity check */
			try {
				PropertyScratchpad scratch = new PropertyScratchpad();
				if (detail != CommandStack.PRE_UNDO) {
					commandChange.simulate(scratch, getRedex());
				} else commandChange.inverse().simulate(scratch, getRedex());
				/* scratch now contains the prospective state of the redex
				 * after the change has been applied. Check that we can still
				 * get to the reactum from there */
				DescriptorExecutorManager.getInstance().
						tryValidateChange(scratch, getRedex(), cdg);
			} catch (ChangeCreationException cce) {
				throw new RuntimeException("BUG: post-fixup reactum changes " +
						"are completely inconsistent, don't save", cce);
			}
			
			/* cdg will be equal to reactumChanges if the fixup operations made
			 * no changes */
			if (cdg != reactumChanges) {
				reactumChanges.clear();
				reactumChanges.addAll(cdg);
			}
			
			/* Anything that's left in lRedexCDs after the fixups should be
			 * unrelated to the reactum changes, and so should be safe to
			 * apply */
			try {
				if (detail != CommandStack.PRE_UNDO) {
					DescriptorExecutorManager.getInstance().tryApplyChange(
							getReactum(), lRedexCDs);
					safeRedexToReactum.put(commandChange, lRedexCDs);
				} else {
					IChangeDescriptor oldlRedexCDs = 
							safeRedexToReactum.remove(commandChange);
					DescriptorExecutorManager.getInstance().tryApplyChange(
							getReactum(), oldlRedexCDs.inverse());
				}
			} catch (ChangeCreationException cce) {
				throw new RuntimeException("BUG: completely unsafe change " +
						"slipped through the net", cce);
			}
		} else if (target == getReactum()) {
			IChangeDescriptor cd = commandChange;
			if (detail == CommandStack.PRE_UNDO) {
				reactumChanges.remove(cd);
			} else reactumChanges.add(cd);
		}
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
}