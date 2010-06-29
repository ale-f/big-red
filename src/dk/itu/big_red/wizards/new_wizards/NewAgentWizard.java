package dk.itu.big_red.wizards.new_wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.util.Project;


public class NewAgentWizard extends Wizard implements INewWizard {
	private NewAgentWizardPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
			Project.findContainerByPath(null, page.getContainerFullPath());
		if (c != null) {
			try {
				Project.getFile(c, page.getFileName());
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			}
		}
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new NewAgentWizardPage("newAgentWizardPage", selection);
		
		page.setTitle("Agent");
		page.setDescription("Create a new agent in an existing bigraphical reactive system.");
		page.setFileExtension("bigraph-agent");
		
		addPage(page);
	}
}
