package org.bigraph.bigmc.red.bgm;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportWizard extends Wizard implements IImportWizard {
	ImportWizardPage mainPage;

	public ImportWizard() {
		super();
	}

	@Override
	public boolean performFinish() {
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("BigMC import wizard");
		setNeedsProgressMonitor(true);
		mainPage = new ImportWizardPage("Import BigMC file");
	}
	
    @Override
	public void addPages() {
        super.addPages(); 
        addPage(mainPage);        
    }
}
