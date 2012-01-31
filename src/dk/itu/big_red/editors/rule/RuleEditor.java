package dk.itu.big_red.editors.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.PaletteToolbar;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SaveAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.AbstractEditor;
import dk.itu.big_red.editors.bigraph.BigraphEditor;
import dk.itu.big_red.editors.bigraph.BigraphEditorContextMenuProvider;
import dk.itu.big_red.editors.bigraph.ChangePropertySheetEntry;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCopyAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerCutAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPasteAction;
import dk.itu.big_red.editors.bigraph.actions.ContainerPropertiesAction;
import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.import_export.ReactionRuleXMLExport;
import dk.itu.big_red.utilities.ValidationFailedException;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.EditorError;
import dk.itu.big_red.utilities.ui.UI;

public class RuleEditor extends AbstractEditor implements
	ISelectionListener, INullSelectionListener, ISelectionChangedListener,
	ISelectionProvider, CommandStackEventListener {
	private DefaultEditDomain editDomain = new DefaultEditDomain(this);

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
		updateActions(selectionActions);
		
		if (listeners.size() == 0)
			return;
		SelectionChangedEvent e =
				new SelectionChangedEvent(this, getSelection());
		for (ISelectionChangedListener l : listeners)
			l.selectionChanged(e);
	}
	
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
		setSaving(true);
		try {
			IOAdapter io = new IOAdapter();
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	ReactionRuleXMLExport ex = new ReactionRuleXMLExport();
        	
        	ex.setModel(getModel()).setOutputStream(io.getOutputStream()).exportObject();
        	Project.setContents(i.getFile(), io.getInputStream());
        	
    		getCommandStack().markSaveLocation();
    		firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (Exception ex) {
        	if (monitor != null)
        		monitor.setCanceled(true);
        	UI.openError("Unable to save the document.", ex);
        } finally {
        	setSaving(false);
        }
	}

	@Override
	public void doSaveAs() {
		SaveAsDialog d = new SaveAsDialog(getSite().getShell());
		d.setBlockOnOpen(true);
		if (d.open() == Dialog.OK) {
			IFile f = Project.getWorkspaceFile(d.getResult());
			getModel().setFile(f);
			
			setInputWithNotify(new FileEditorInput(f));
			doSave(null);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		redexViewer = new ScrollingGraphicalViewer();
		reactumViewer = new ScrollingGraphicalViewer();
		
		setSite(site);
		setInputWithNotify(input);
	}

	@Override
	public boolean isDirty() {
		return getCommandStack().isDirty();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	private Composite parent, self;
	
	private void error(Throwable t) {
		self.dispose(); self = null;
		new EditorError(parent, RedPlugin.getThrowableStatus(t));
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
	
	protected void loadInput() {
		IEditorInput input = getEditorInput();
		if (input instanceof FileEditorInput) {
	    	FileEditorInput fi = (FileEditorInput)input;
	    	try {
	    		setModel((ReactionRule)Import.fromFile(fi.getFile()));
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
	    
	    BigraphEditor.updateNodePalette(nodeGroup,
	    	model.getRedex().getSignature());
	    
	    redexViewer.setContents(model.getRedex());
	    reactumViewer.setContents(model.getReactum());
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
				cre.killVM();
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
		
		firePropertyChange(IEditorPart.PROP_DIRTY);
		updateActions(stackActions);
	}

	@Override
	protected void createActions() {
		ActionRegistry registry = getActionRegistry();
		
		AbstractEditor.registerActions(registry, stackActions,
			new UndoAction(this), new RedoAction(this));
		
		AbstractEditor.registerActions(registry, selectionActions,
			new DeleteAction((IWorkbenchPart)this),
			new ContainerPropertiesAction(this), new ContainerCutAction(this),
			new ContainerCopyAction(this), new BigraphRelayoutAction(this),
			new ContainerPasteAction(this));

		AbstractEditor.registerActions(registry, null,
			new SelectAllAction(this));
		
		AbstractEditor.registerActions(registry, propertyActions,
			new SaveAction(this));
	}

	@Override
	protected void initializeActionRegistry() {
		super.initializeActionRegistry();
		updateActions(propertyActions);
		updateActions(stackActions);
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == CommandStack.class) {
			return getCommandStack();
		} else if (adapter == IPropertySheetPage.class) {
			PropertySheetPage psp = new PropertySheetPage();
			psp.setRootEntry(new ChangePropertySheetEntry(getCommandStack()));
			return psp;
		} else if (adapter == CommandStack.class) {
			return getCommandStack();
		} else return super.getAdapter(adapter);
	}
}