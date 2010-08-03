package dk.itu.big_red.wizards.creation;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.exceptions.ImportFailedException;
import dk.itu.big_red.import_export.assistants.FileResourceOutputStream;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.import_export.BigraphXMLExport;
import dk.itu.big_red.model.import_export.SignatureXMLImport;
import dk.itu.big_red.util.Project;
import dk.itu.big_red.util.UI;
import dk.itu.big_red.wizards.creation.assistants.WizardNewAgentCreationPage;

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
				IFile sigFile =
					Project.findFileByPath(null, page.getSignaturePath());
				IFile bigFile = Project.getFile(c, page.getFileName());
				Bigraph b = new Bigraph();
				b.setSignature(sigFile, new SignatureXMLImport().setInputStream(sigFile.getContents()).importObject());
				new BigraphXMLExport().setModel(b).setOutputStream(new FileResourceOutputStream(bigFile)).exportObject();
				UI.openInEditor(bigFile);
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (ImportFailedException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (ExportFailedException e) {
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
