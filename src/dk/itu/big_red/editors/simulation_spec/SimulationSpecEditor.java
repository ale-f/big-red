package dk.itu.big_red.editors.simulation_spec;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
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
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.import_export.BigraphXMLImport;
import dk.itu.big_red.model.import_export.ReactionRuleXMLImport;
import dk.itu.big_red.model.import_export.SignatureXMLImport;
import dk.itu.big_red.model.import_export.SimulationSpecXMLImport;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog.Mode;
import dk.itu.big_red.utilities.ui.ResourceSelector;
import dk.itu.big_red.utilities.ui.ResourceSelector.ResourceListener;
import dk.itu.big_red.utilities.ui.UI;

public class SimulationSpecEditor extends EditorPart {

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInputWithNotify(input);
		
		loadInput();
	}

	private SimulationSpec model = null;
	
	protected SimulationSpec getModel() {
		return model;
	}
	
	protected void loadInput() {
		IEditorInput input = getEditorInput();
		if (input instanceof FileEditorInput) {
			FileEditorInput fi = (FileEditorInput)input;
			try {
				model = SimulationSpecXMLImport.importFile(fi.getFile());
			} catch (ImportFailedException ex) {
				
			}
		}
		if (model == null)
			model = new SimulationSpec();
		setPartName(input.getName());
	}
	
	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private Export<SimulationSpec> getExporter(IConfigurationElement e) {
		try {
			return
				(Export<SimulationSpec>)e.createExecutableExtension("class");
		} catch (CoreException ex) {
			return null;
		}
	}
	
	private Export<SimulationSpec> getExporter(String id) {
		return getExporter(getExporters().get(id));
	}
	
	private static Map<String, IConfigurationElement> getExporters() {
		Map<String, IConfigurationElement> exporters =
				new HashMap<String, IConfigurationElement>();
		for (IConfigurationElement ce :
		     RedPlugin.getConfigurationElementsFor("dk.itu.big_red.export.text")) {
			String exports = ce.getAttribute("exports");
			if (exports.equals("dk.itu.big_red.model.SimulationSpec"))
				exporters.put(ce.getAttribute("name"), ce);
		}
		return exporters;
	}
	
	private ResourceSelector signatureSelector, modelSelector;
	
	@Override
	public void createPartControl(Composite parent) {
		Composite base = new Composite(parent, SWT.NONE);
		base.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		base.setLayout(new GridLayout(4, false));
		
		UI.newLabel(base, SWT.RIGHT, "Signature:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		signatureSelector = new ResourceSelector(base,
			((FileEditorInput)getEditorInput()).getFile().getProject(),
			Mode.FILE, "dk.itu.big_red.signature");
		signatureSelector.getButton().setLayoutData(
			new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		signatureSelector.addListener(new ResourceListener() {
			@Override
			public void resourceChanged(IResource newValue) {
				try {
					Signature s = SignatureXMLImport.importFile((IFile)newValue);
					getModel().tryApplyChange(getModel().changeSignature(s));
				} catch (ChangeRejectedException cre) {
					cre.printStackTrace();
				} catch (ImportFailedException ife) {
					ife.printStackTrace();
				}
			}
		});
		
		UI.newLabel(base, SWT.RIGHT, "Reaction rules:").setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		final Tree rules =
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
						model.tryApplyChange(model.changeAddRule(r));
						
						TreeItem t = UI.data(
								new TreeItem(rules, SWT.NONE),
								"associatedRule", r);
						t.setText(f.getProjectRelativePath().toString());
					} catch (ImportFailedException ife) {
						ife.printStackTrace();
					} catch (ChangeRejectedException cre) {
						cre.printStackTrace();
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
					try {
						ReactionRule rr =
								(ReactionRule)UI.data(i, "associatedRule");
						getModel().tryApplyChange(getModel().changeRemoveRule(rr));
						UI.data(i, "associatedRule", null).dispose();
					} catch (ChangeRejectedException cre) {
						cre.printStackTrace();
					}
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
			public void resourceChanged(IResource newValue) {
				try {
					Bigraph b = BigraphXMLImport.importFile((IFile)newValue);
					getModel().tryApplyChange(getModel().changeModel(b));
				} catch (ChangeRejectedException cre) {
					cre.printStackTrace();
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
		
		b = UI.newButton(base, SWT.NONE, "Two thing(s)...");
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Export<SimulationSpec> exporter = getExporter(c.getText());
				System.out.println(exporter);
				try {
					IOAdapter io = new IOAdapter();
					exporter.setModel(getModel()).
						setOutputStream(io.getOutputStream()).exportObject();
				} catch (ExportFailedException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		UI.getWorkbenchPage().activate(this);
	}

}
