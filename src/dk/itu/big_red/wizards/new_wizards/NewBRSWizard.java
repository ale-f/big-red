package dk.itu.big_red.wizards.new_wizards;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import dk.itu.big_red.util.Project;

public class NewBRSWizard extends Wizard implements INewWizard {
	private WizardNewProjectCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		if (Project.projectExists(page.getProjectName())) {
			page.setErrorMessage("A project with this name already exists.");
			return false;
		} else {
			try {
				IProject p = Project.getProject(page.getProjectName());
				IFolder signatures = Project.getFolder(p, "signatures"),
				        agents = Project.getFolder(p, "agents"),
				        rules = Project.getFolder(p, "rules");
				Project.getFile(signatures, page.getProjectName() + ".bigraph-signature");
				Project.getFile(agents, "test.bigraph-agent");
				Project.getFile(p, page.getProjectName() + ".bigraph-simulation-spec");
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
				return false;
			}
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new WizardNewProjectCreationPage("newBRSWizardPage");
		
		page.setTitle("Bigraphical reactive system");
		page.setDescription("Create a new bigraphical reactive system.");
		
		addPage(page);
	}
}
