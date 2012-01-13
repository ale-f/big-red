package dk.itu.big_red.wizards.creation;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;

public class NewSimulationSpecWizard extends Wizard implements INewWizard {
	private WizardNewFileCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
			Project.findContainerByPath(null, page.getContainerFullPath());
		if (c != null) {
			try {
				UI.openInEditor(Project.getFile(c, page.getFileName()));
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		page = new WizardNewFileCreationPage("newSimulationSpecWizardPage", selection);
		
		page.setTitle("Simulation spec");
		page.setDescription("Create a simulation spec for an existing bigraphical reactive system.");
		page.setFileExtension("bigraph-simulation-spec");
		
		addPage(page);
	}
	
	public static void createSimulationSpec(IFile ssFile) throws ExportFailedException, CoreException {
		/* do nothing (for now) */
	}
}
