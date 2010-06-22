package dk.itu.big_red.wizards.new_wizards;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewSimulationSpecWizard extends Wizard implements INewWizard {
	private NewSimulationSpecWizardPage page = null;
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		page = new NewSimulationSpecWizardPage("newSimulationSpecWizardPage", selection);
		
		page.setTitle("Simulation spec");
		page.setDescription("Create a simulation spec for an existing bigraphical reactive system.");
		
		addPage(page);
	}

}
