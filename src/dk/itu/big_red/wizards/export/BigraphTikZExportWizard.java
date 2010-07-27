package dk.itu.big_red.wizards.export;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.import_export.BigraphTikZExport;
import dk.itu.big_red.model.import_export.BigraphXMLImport;
import dk.itu.big_red.util.Project;
import dk.itu.big_red.wizards.export.assistants.WizardBigraphTikZExportPage;

public class BigraphTikZExportWizard extends Wizard implements IExportWizard {
	private WizardBigraphTikZExportPage page = null;
	
	@Override
	public boolean performFinish() {
		Bigraph model;
		
		BigraphXMLImport im = new BigraphXMLImport();
		try {
			im.setInputStream(Project.findFileByPath(null, page.getBigraphPath()).getContents());
			model = im.importObject();
		} catch (Exception e) {
			page.setErrorMessage(e.getLocalizedMessage());
			return false;
		}
		
		BigraphTikZExport ex = new BigraphTikZExport();
		try {
			ex.setModel(model);
			ex.setOutputFile(page.getTargetPath());
			ex.exportObject();
		} catch (Exception e) {
			page.setErrorMessage(e.getLocalizedMessage());
			return false;
		}
		
		return true;
	}
	
	@Override
	public void init(IWorkbench workbench, final IStructuredSelection selection) {
		page = new WizardBigraphTikZExportPage("bigraphTikZExportPage", selection);
		
		page.setTitle("Export as TikZ image");
		page.setDescription("Export the current bigraph as a TikZ image, suitable for use in papers or high-resolution printing.");
		
		addPage(page);
	}

}
