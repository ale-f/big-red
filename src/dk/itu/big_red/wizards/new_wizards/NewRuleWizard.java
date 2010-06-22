package dk.itu.big_red.wizards.new_wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


public class NewRuleWizard extends Wizard implements INewWizard {
	private NewRuleWizardPage page = null;
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new NewRuleWizardPage("newRuleWizardPage", selection);
		
		page.setTitle("Rule");
		page.setDescription("Create a new rule in an existing bigraphical reactive system.");
		
		addPage(page);
	}
}
