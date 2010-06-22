package dk.itu.big_red.wizards.new_wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


public class NewAgentWizard extends Wizard implements INewWizard {
	private NewAgentWizardPage page = null;
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new NewAgentWizardPage("newAgentWizardPage", selection);
		
		page.setTitle("Agent");
		page.setDescription("Create a new agent in an existing bigraphical reactive system.");
		
		addPage(page);
	}
}
