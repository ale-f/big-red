package dk.itu.big_red.editors.simulation_spec;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.utilities.resources.ResourceTreeSelectionDialog;
import dk.itu.big_red.utilities.ui.ResourceSelector;
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
	private static Map<String, Export<SimulationSpec>> getExporters() {
		Map<String, Export<SimulationSpec>> exporters =
				new HashMap<String, Export<SimulationSpec>>();
		for (IConfigurationElement ce :
		     RedPlugin.getConfigurationElementsFor("dk.itu.big_red.export.text")) {
			try {
				String id = ce.getAttribute("name");
				Object exporter_ = ce.createExecutableExtension("class");
				if (exporter_ instanceof Export<?>) {
					Export<?> exporter = (Export<?>)exporter_;
					if (exporter.getType() == SimulationSpec.class)
						exporters.put(id, (Export<SimulationSpec>)exporter);
				}
			} catch (CoreException e) {
				
			}
		}
		return exporters;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		Composite base = new Composite(parent, SWT.NONE);
		base.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		base.setLayout(new GridLayout(4, false));
		
		UI.newLabel(base, SWT.RIGHT, "Signature:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		new ResourceSelector(base,
			((FileEditorInput)getEditorInput()).getFile().getProject(),
			ResourceTreeSelectionDialog.MODE_FILE,
			"dk.itu.big_red.signature").
			getButton().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		UI.newLabel(base, SWT.RIGHT, "Reaction rules:").setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		new List(base, SWT.BORDER).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
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
		
		b = UI.newButton(br, SWT.NONE, "&Remove...");
		b.setImage(UI.getImage(ISharedImages.IMG_ELCL_REMOVE));
		
		UI.newLabel(base, SWT.RIGHT, "Model:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		new ResourceSelector(base,
				((FileEditorInput)getEditorInput()).getFile().getProject(),
				ResourceTreeSelectionDialog.MODE_FILE,
				"dk.itu.big_red.bigraph").
				getButton().setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		
		new Label(base, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		
		UI.newLabel(base, SWT.RIGHT, "Tool:").setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		Combo c = new Combo(base, SWT.READ_ONLY);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		String[] exporters = getExporters().keySet().toArray(new String[0]);
		c.setItems(exporters);
		c.setText(exporters[0]);
		
		b = UI.newButton(base, SWT.NONE, "Two thing(s)...");
		b.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		UI.getWorkbenchPage().activate(this);
	}

}
