package dk.itu.big_red.editors.rule;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
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
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import dk.itu.big_red.editors.AbstractGEFEditor;
import dk.itu.big_red.editors.bigraph.BigraphEditor;
import dk.itu.big_red.editors.bigraph.BigraphEditorContextMenuProvider;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.import_export.ReactionRuleXMLExport;
import dk.itu.big_red.utilities.ValidationFailedException;
import dk.itu.big_red.utilities.ui.UI;

public class RuleEditor extends AbstractGEFEditor implements
	ISelectionChangedListener, ISelectionProvider {
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
	public void doActualSave(OutputStream os) throws ExportFailedException {
    	new ReactionRuleXMLExport().setModel(getModel()).
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
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setComposite(c);
		
		GridLayout gl = new GridLayout(3, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		c.setLayout(gl);
		
		createPaletteViewer(c);
		getPaletteViewer().getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		getEditDomain().setPaletteViewer(getPaletteViewer());
		
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
		
		redexViewer.setEditDomain(getEditDomain());
		reactumViewer.setEditDomain(getEditDomain());
		
		redexViewer.setEditPartFactory(new PartFactory());
		reactumViewer.setEditPartFactory(new PartFactory());
		
		redexViewer.setRootEditPart(new ScalableRootEditPart());
		reactumViewer.setRootEditPart(new ScalableRootEditPart());
		
		redexViewer.setContextMenu(
			new BigraphEditorContextMenuProvider(redexViewer, getActionRegistry()));
		reactumViewer.setContextMenu(
			new BigraphEditorContextMenuProvider(reactumViewer, getActionRegistry()));
		
		redexViewer.addSelectionChangedListener(this);
		reactumViewer.addSelectionChangedListener(this);
		getSite().setSelectionProvider(this);
		
		getCommandStack().addCommandStackEventListener(this);
		
		initialise();
	}
	
	private PaletteGroup nodeGroup;
	
	@Override
	protected PaletteRoot getPaletteRoot() {
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
	
	@Override
	public ReactionRule getModel() {
		return model;
	}
	
	public void setModel(ReactionRule model) {
		this.model = model;
	}
	
	@Override
	protected void initialiseActual() throws Throwable {
		IEditorInput input = getEditorInput();
		if (input instanceof FileEditorInput) {
	    	FileEditorInput fi = (FileEditorInput)input;
	    	try {
	    		setModel((ReactionRule)Import.fromFile(fi.getFile()));
	    	} catch (ImportFailedException e) {
	    		e.printStackTrace();
	    		Throwable cause = e.getCause();
	    		if (cause instanceof ValidationFailedException) {
	    			throw cause;
	    		} else throw e;
	    	}
	    }
	    
	    BigraphEditor.updateNodePalette(nodeGroup,
	    	model.getRedex().getSignature());
	    
	    redexViewer.setContents(model.getRedex());
	    reactumViewer.setContents(model.getReactum());
    }
	
	private Bigraph getRedex() {
		return (Bigraph)redexViewer.getContents().getModel();
	}
	
	private Bigraph getReactum() {
		return (Bigraph)reactumViewer.getContents().getModel();
	}
	
	private Map<Change, Change> redexChangesToReactumChanges =
			new HashMap<Change, Change>();
	
	private Change getReactumChange(Change redexChange) {
		Change reactumChange = redexChangesToReactumChanges.get(redexChange);
		if (reactumChange == null) {
			reactumChange =
				ReactionRule.translateChange(getModel().getRedexToReactumMap(), redexChange);
			redexChangesToReactumChanges.put(redexChange, reactumChange);
		}
		return reactumChange;
	}
	
	private void processChangeCommand(int detail, ChangeCommand c) {
		IChangeable target = c.getTarget();
		
		if (target == getRedex()) {
			Change reactumChange = getReactumChange(c.getChange());
			if (detail == CommandStack.POST_UNDO)
				reactumChange = reactumChange.inverse();
			try {
				getReactum().tryApplyChange(reactumChange);
			} catch (ChangeRejectedException cre) {
				throw new Error("Unhandled Change failure", cre);
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
				@SuppressWarnings("unchecked")
				List<Command> cmds = ((CompoundCommand)c).getCommands();
				for (Command i : cmds)
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
	protected void initializeActionRegistry() {
		super.initializeActionRegistry();
		updateActions(getStateActions());
	}
	
	@Override
	public void setFocus() {
		if (getComposite() == null)
			return;
		getComposite().setFocus();
	}
}