package dk.itu.big_red.wizards.export;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.model.import_export.BigraphTikZExport;
import dk.itu.big_red.wizards.export.assistants.WizardBigraphTextExportPage;

public class BigraphTikZExportWizard extends Wizard implements IExportWizard {
	private WizardBigraphTextExportPage page = null;
	
	@Override
	public boolean performFinish() {
		return true;
	}
	
	@Override
	public void init(IWorkbench workbench, final IStructuredSelection selection) {
		page = new WizardBigraphTextExportPage("bigraphTikZExportPage", selection, BigraphTikZExport.class);
		page.setTitle("Export as TikZ image");
		page.setDescription("Export the current bigraph as a TikZ image, suitable for use in papers or high-resolution printing.");
		setWindowTitle("Export as TikZ image");
		
		addPage(page); 
	}

}
