package dk.itu.big_red.wizards.export;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.wizards.export.assistants.WizardBigraphTikZExportPage;

public class BigraphTikZExportWizard extends Wizard implements IExportWizard {
	private WizardBigraphTikZExportPage page = null;
	
	@Override
	public boolean performFinish() {
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, final IStructuredSelection selection) {
		page = new WizardBigraphTikZExportPage("bigraphTikZExportPage", selection);
		
		page.setTitle("Export as TikZ image");
		page.setDescription("Export the current bigraph as a TikZ image, suitable for use in papers or high-resolution printing.");
		
		addPage(page);
	}

}
