package dk.itu.big_red.wizards.creation;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.savers.SignatureXMLSaver;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;

public class NewSignatureWizard extends Wizard implements INewWizard {
	private WizardNewFileCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
			Project.findContainerByPath(null, page.getContainerFullPath());
		if (c != null) {
			try {
				IFile sigFile = Project.getFile(c, page.getFileName());
				NewSignatureWizard.createSignature(sigFile);
				UI.openInEditor(sigFile);
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (SaveFailedException e) {
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

	protected static void createSignature(IFile sigFile)
			throws SaveFailedException, CoreException {
		IOAdapter io = new IOAdapter();
		
		new SignatureXMLSaver().setModel(new Signature()).setFile(sigFile).
			setOutputStream(io.getOutputStream()).exportObject();
		sigFile.setContents(io.getInputStream(), 0, null);
	}
}
