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
import dk.itu.big_red.util.UI;
import dk.itu.big_red.util.resources.Project;

public class NewSignatureWizard extends Wizard implements INewWizard {
	private WizardNewFileCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
			Project.findContainerByPath(null, page.getContainerFullPath());
		if (c != null) {
			try {
				IFile sigFile = Project.getFile(c, page.getFileName());
				Project.createSignature(sigFile);
				UI.openInEditor(sigFile);
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (ExportFailedException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			}
		}
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new WizardNewFileCreationPage("newSignatureWizardPage", selection);
		
		page.setTitle("Signature");
		page.setDescription("Create a new signature in an existing bigraphical reactive system.");
		page.setFileExtension("bigraph-signature");
		
		addPage(page);
	}
}
