package dk.itu.big_red.wizards.new_wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

import dk.itu.big_red.util.Project;
import dk.itu.big_red.util.UI;
import dk.itu.big_red.wizards.new_wizards.assistants.WizardNewAgentCreationPage;

/**
 * NewAgentWizards are responsible for creating {@link Bigraph} files within a
 * project.
 * @author alec
 *
 */
public class NewAgentWizard extends Wizard implements INewWizard {
	private WizardNewAgentCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
			Project.findContainerByPath(null, page.getFolderPath());
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
		page = new WizardNewAgentCreationPage("newAgentWizardPage", selection);
		
		page.setTitle("Agent");
		page.setDescription("Create a new agent in an existing bigraphical reactive system.");
		
		addPage(page);
	}
}
