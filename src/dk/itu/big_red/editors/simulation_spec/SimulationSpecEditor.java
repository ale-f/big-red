package dk.itu.big_red.editors.simulation_spec;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.AbstractEditor;
import dk.itu.big_red.editors.assistants.RedoProxyAction;
import dk.itu.big_red.editors.assistants.UndoProxyAction;
import dk.itu.big_red.editors.assistants.RedoProxyAction.IRedoImplementor;
import dk.itu.big_red.editors.assistants.UndoProxyAction.IUndoImplementor;
import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.import_export.BigraphXMLImport;
import dk.itu.big_red.model.import_export.ReactionRuleXMLImport;
import dk.itu.big_red.model.import_export.SignatureXMLImport;
import dk.itu.big_red.model.import_export.SimulationSpecXMLExport;
import dk.itu.big_red.model.import_export.SimulationSpecXMLImport;
import dk.itu.big_red.utilities.ValidationFailedException;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;
import dk.itu.big_red.utilities.ui.EditorError;
import dk.itu.big_red.utilities.ui.ResourceSelector;
import dk.itu.big_red.utilities.ui.ResourceSelector.ResourceListener;
import dk.itu.big_red.utilities.ui.UI;

public class SimulationSpecEditor extends AbstractEditor
implements IUndoImplementor, IRedoImplementor, PropertyChangeListener {
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			IOAdapter io = new IOAdapter();
        	FileEditorInput i = (FileEditorInput)getEditorInput();
        	SimulationSpecXMLExport ex = new SimulationSpecXMLExport();
        	
        	ex.setModel(getModel()).setOutputStream(io.getOutputStream()).exportObject();
        	Project.setContents(i.getFile(), io.getInputStream());
        	
    		savePoint = undoBuffer.peek();
    		checkDirt();
        } catch (Exception ex) {
        	if (monitor != null)
        		monitor.setCanceled(true);
        	UI.openError("Unable to save the document.", ex);
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
		setSite(site);
		setInputWithNotify(input);
	}

	private Change savePoint = null;
	private ArrayDeque<Change>
			undoBuffer = new ArrayDeque<Change>(),
			redoBuffer = new ArrayDeque<Change>();
	
	@Override
	public boolean canUndo() {
		return (undoBuffer.size() != 0);
	}
	
	@Override
	public boolean canRedo() {
		return (redoBuffer.size() != 0);
	}
	
	private void doChange(Change c) {
		try {
			model.tryApplyChange(c);
			redoBuffer.clear();
			undoBuffer.push(c);
		} catch (ChangeRejectedException cre) {
			cre.killVM();
		}
		checkDirt();
		updateActions(stackActions);
	}
	
	@Override
	public void undo() {
		try {
			if (!canUndo())
				return;
			Change c;
			redoBuffer.push(c = undoBuffer.pop());
			model.tryApplyChange(c.inverse());
		} catch (ChangeRejectedException cre) {
			/* should never happen */
			cre.killVM();
		}
		checkDirt();
		updateActions(stackActions);
	}
	
	@Override
	public void redo() {
		try {
			if (!canRedo())
				return;
			Change c;
			model.tryApplyChange(c = redoBuffer.pop());
			undoBuffer.push(c);
		} catch (ChangeRejectedException cre) {
			/* should never happen */
			cre.killVM();
		}
		checkDirt();
		updateActions(stackActions);
	}
	
	private void checkDirt() {
		boolean newDirty = (undoBuffer.peek() != savePoint);
		if (newDirty != dirty) {
			dirty = newDirty;
			firePropertyChange(PROP_DIRTY);
		}
	}
	
	private SimulationSpec model = null;
	
	protected SimulationSpec getModel() {
		return model;
	}
	
	private boolean uiUpdateInProgress = false;
	
	protected void initialiseSimulationSpecEditor() {
		IEditorInput input = getEditorInput();
		if (input instanceof FileEditorInput) {
			FileEditorInput fi = (FileEditorInput)input;
			try {
				model = SimulationSpecXMLImport.importFile(fi.getFile());
			} catch (ImportFailedException e) {
	    		e.printStackTrace();
	    		Throwable cause = e.getCause();
	    		if (cause instanceof ValidationFailedException) {
	    			error(cause);
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
		if (model == null)
			model = new SimulationSpec();
		
		model.addPropertyChangeListener(this);
		modelToControls();
	}
	
	private void modelToControls() {
		uiUpdateInProgress = true;
		
		Signature s = model.getSignature();
		if (s != null)
			signatureSelector.setResource(s.getFile());
		
		for (ReactionRule r : model.getRules()) {
			TreeItem t = UI.data(
				new TreeItem(rules, SWT.NONE),
				"associatedRule", r);
			t.setText(r.getFile().getProjectRelativePath().toString());
		}
		
		Bigraph b = model.getModel();
		if (b != null)
			modelSelector.setResource(b.getFile());
		
		uiUpdateInProgress = false;
	}
	
	private boolean dirty = false;
	
	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private Export<SimulationSpec> getExporter(IConfigurationElement e) {
		return (Export<SimulationSpec>)RedPlugin.instantiate(e);
	}
	
	private Export<SimulationSpec> getExporter(String id) {
		return getExporter(getExporters().get(id));
	}
	
	private static Map<String, IConfigurationElement> getExporters() {
		Map<String, IConfigurationElement> exporters =
				new HashMap<String, IConfigurationElement>();
		for (IConfigurationElement ce :
		     RedPlugin.getConfigurationElementsFor("dk.itu.big_red.export")) {
			String exports = ce.getAttribute("exports");
			if (exports.equals(SimulationSpec.class.getCanonicalName()))
				exporters.put(ce.getAttribute("name"), ce);
		}
		return exporters;
	}
	
	private ResourceSelector signatureSelector, modelSelector;
	private Tree rules;
	private Button export;
	
	private Composite parent, self;
	
	private void error(Throwable t) {
		self.dispose(); self = null;
		new EditorError(parent, RedPlugin.getThrowableStatus(t));
	}
	
	private void recalculateExportEnabled() {
		export.setEnabled(
			signatureSelector.getResource() != null &&
			modelSelector.getResource() != null);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		self = new Composite(parent, SWT.NONE);
		
		Composite base = self;
		base.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout gl = new GridLayout(4, false);
		gl.marginTop = gl.marginLeft = gl.marginBottom = gl.marginRight = 
			gl.horizontalSpacing = gl.verticalSpacing = 10;
		base.setLayout(gl);
		
		UI.newLabel(base, SWT.RIGHT, "Signature:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		signatureSelector = new ResourceSelector(base,
			((FileEditorInput)getEditorInput()).getFile().getProject(),
			Mode.FILE, "dk.itu.big_red.signature");
		signatureSelector.getButton().setLayoutData(
			new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		signatureSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				recalculateExportEnabled();
				try {
					if (uiUpdateInProgress)
						return;
					Signature s = null;
					if (newValue != null)
						s = SignatureXMLImport.importFile((IFile)newValue);
					doChange(getModel().changeSignature(s));
				} catch (ImportFailedException ife) {
					ife.printStackTrace();
				}
			}
		});
		
		UI.newLabel(base, SWT.RIGHT, "Reaction rules:").setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		rules =
			UI.setLayoutData(new Tree(base, SWT.BORDER | SWT.MULTI),
				new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite br = new Composite(base, SWT.NONE);
		br.setBackground(ColorConstants.red);
		br.setLayoutData(new GridData(SWT.END, SWT.BOTTOM, false, false, 2, 1));
		RowLayout brl = new RowLayout(SWT.VERTICAL);
		brl.marginBottom = brl.marginLeft = brl.marginRight =
				brl.marginTop = 0;
		brl.pack = false;
		br.setLayout(brl);
		
		Button b = UI.newButton(br, SWT.NONE, "&Add...");
		b.setImage(UI.getImage(ISharedImages.IMG_OBJ_ADD));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ResourceTreeSelectionDialog rtsd =
					new ResourceTreeSelectionDialog(
						getSite().getShell(),
						((FileEditorInput)getEditorInput()).getFile().getProject(),
						Mode.FILE, "dk.itu.big_red.rule");
				rtsd.setBlockOnOpen(true);
				if (rtsd.open() == Dialog.OK) {
					IFile f = (IFile)rtsd.getFirstResult();
					try {
						ReactionRule r = ReactionRuleXMLImport.importFile(f);
						doChange(model.changeAddRule(r));
					} catch (ImportFailedException ife) {
						ife.printStackTrace();
					}
				}
			}
		});
		
		b = UI.newButton(br, SWT.NONE, "&Remove...");
		b.setImage(UI.getImage(ISharedImages.IMG_ELCL_REMOVE));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (TreeItem i : rules.getSelection()) {
					ReactionRule rr =
							(ReactionRule)UI.data(i, "associatedRule");
					doChange(getModel().changeRemoveRule(rr));
				}
			}
		});
		
		UI.newLabel(base, SWT.RIGHT, "Model:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		modelSelector = new ResourceSelector(base,
			((FileEditorInput)getEditorInput()).getFile().getProject(),
			Mode.FILE, "dk.itu.big_red.bigraph");
		modelSelector.getButton().setLayoutData(
			new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		modelSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource oldValue, IResource newValue) {
				recalculateExportEnabled();
				try {
					if (uiUpdateInProgress)
						return;
					Bigraph b = null;
					if (newValue != null)
						b = BigraphXMLImport.importFile((IFile)newValue);
					doChange(getModel().changeModel(b));
				} catch (ImportFailedException ife) {
					ife.printStackTrace();
				}
			}
		});
		
		new Label(base, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		UI.newLabel(base, SWT.RIGHT, "Tool:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		final Combo c = new Combo(base, SWT.READ_ONLY);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		String[] exporters = getExporters().keySet().toArray(new String[0]);
		c.setItems(exporters);
		c.setText(exporters[0]);
		
		export = UI.newButton(base, SWT.NONE, "&Export...");
		export.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		export.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Export<SimulationSpec> exporter = getExporter(c.getText());
				System.out.println(exporter);
				try {
					IOAdapter io = new IOAdapter();
					exporter.setModel(getModel()).
						setOutputStream(io.getOutputStream()).exportObject();
					SimulationSpecUIFactory.
						createResultsWindow(
							getSite().getShell(),
							IOAdapter.readString(io.getInputStream())).
						open();
				} catch (ExportFailedException ex) {
					ex.printStackTrace();
				}
			}
		});
		export.setEnabled(false);
		
		initialiseSimulationSpecEditor();
	}

	private ArrayList<String> stackActions = new ArrayList<String>();
	
	@Override
	protected void createActions() {
		ActionRegistry registry = getActionRegistry();
		
		registerActions(registry, stackActions,
				new UndoProxyAction(this), new RedoProxyAction(this));
	}
	
	@Override
	protected void initializeActionRegistry() {
		super.initializeActionRegistry();
		updateActions(stackActions);
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		UI.getWorkbenchPage().activate(this);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() != getModel() || uiUpdateInProgress)
			return;
		uiUpdateInProgress = true;
		try {
			String propertyName = evt.getPropertyName();
			Object oldValue = evt.getOldValue(),
					newValue = evt.getNewValue();
			uiUpdateInProgress = true;
			if (propertyName.equals(SimulationSpec.PROPERTY_SIGNATURE)) {
				Signature s = (Signature)newValue;
				signatureSelector.setResource((s != null ? s.getFile() : null));
			} else if (propertyName.equals(SimulationSpec.PROPERTY_RULE)) {
				if (oldValue == null && newValue != null) { /* added */
					ReactionRule r = (ReactionRule)newValue;
					TreeItem t = UI.data(
							new TreeItem(rules, SWT.NONE),
							"associatedRule", r);
					t.setText(r.getFile().getProjectRelativePath().toString());
				} else if (oldValue != null && newValue == null) { /* removed */
					ReactionRule r = (ReactionRule)oldValue;
					for (TreeItem i : rules.getItems()) {
						if (r.equals(UI.data(i, "associatedRule"))) {
							UI.data(i, "associatedRule", null).dispose();
							break;
						}
					}
				}
			} else if (propertyName.equals(SimulationSpec.PROPERTY_MODEL)) {
				Bigraph b = (Bigraph)newValue;
				modelSelector.setResource((b != null ? b.getFile() : null));
			}
		} finally {
			uiUpdateInProgress = false;
		}
	}
}
