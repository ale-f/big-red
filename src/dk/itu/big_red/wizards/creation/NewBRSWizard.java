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

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.util.resources.Project;

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
				        agents = Project.getFolder(p, "agents");
				IFile signature = Project.getFile(signatures, page.getProjectName() + ".bigraph-signature");
				Project.createSignature(signature);
				Project.createBigraph(signature, Project.getFile(agents, page.getProjectName() + ".bigraph-agent"));
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (ImportFailedException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (ExportFailedException e) {
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
