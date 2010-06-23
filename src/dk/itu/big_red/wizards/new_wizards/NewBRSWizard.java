package dk.itu.big_red.wizards.new_wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.util.NullInputStream;

public class NewBRSWizard extends Wizard implements INewWizard {
	private NewBRSWizardPage page = null;
	
	@Override
	public boolean performFinish() {
		IWorkspaceRoot r = ResourcesPlugin.getWorkspace().getRoot();
		IProject p = r.getProject(page.getProjectName());
		if (p.exists()) {
			page.setErrorMessage("A project with this name already exists.");
			return false;
		} else {
			try {
				p.create(null);
				p.open(null);
				p.getFolder("signatures").create(0, true, null);
				p.getFolder("signatures").getFile(page.getProjectName() + ".bigraph-signature").create(NullInputStream.getInstance(), 0, null);
				p.getFolder("agents").create(0, true, null);
				p.getFolder("agents").getFile("test.bigraph-agent").create(NullInputStream.getInstance(), 0, null);
				p.getFolder("rules").create(0, true, null);
				p.getFile(page.getProjectName() + ".bigraph-simulation-spec").create(NullInputStream.getInstance(), 0, null);
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
				return false;
			}
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		page = new NewBRSWizardPage("newBRSWizardPage");
		
		page.setTitle("Bigraphical reactive system");
		page.setDescription("Create a new bigraphical reactive system.");
		
		addPage(page);
	}
}
