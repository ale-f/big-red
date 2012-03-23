package dk.itu.big_red.wizards.creation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.utilities.resources.Project;

public class NewBRSWizard extends Wizard implements INewWizard {
	private WizardNewProjectCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IProject p =
			Project.getWorkspaceRoot().getProject(page.getProjectName());
		if (p.exists()) {
			page.setErrorMessage("A project with this name already exists.");
			return false;
		} else {
			try {
				p.create(null); p.open(null);
				IFolder signatures = Project.getFolder(p, "signatures"),
				        agents = Project.getFolder(p, "agents");
				Project.getFolder(p, "rules");
				
				IFile signature =
					Project.getFile(signatures,
						page.getProjectName() + ".bigraph-signature");
				NewSignatureWizard.createSignature(signature);
				NewAgentWizard.createBigraph(signature,
					Project.getFile(agents,
						page.getProjectName() + ".bigraph-agent"));
				NewSimulationSpecWizard.createSimulationSpec(
					Project.getFile(p,
						page.getProjectName() + ".bigraph-simulation-spec"));
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (LoadFailedException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (SaveFailedException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			}
			return false;
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