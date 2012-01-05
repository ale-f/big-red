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
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.import_export.ReactionRuleXMLExport;
import dk.itu.big_red.model.import_export.SignatureXMLImport;
import dk.itu.big_red.util.UI;
import dk.itu.big_red.util.io.IOAdapter;
import dk.itu.big_red.util.resources.Project;


public class NewRuleWizard extends Wizard implements INewWizard {
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
		page = new WizardNewFileCreationPage("newRuleWizardPage", selection);
		
		page.setTitle("Rule");
		page.setDescription("Create a new rule in an existing bigraphical reactive system.");
		page.setFileExtension("bigraph-rule");
		
		addPage(page);
	}

	public static void createReactionRule(IFile sigFile, IFile rrFile) throws ImportFailedException, ExportFailedException, CoreException {
		IOAdapter io = new IOAdapter();
		
		ReactionRule rr = new ReactionRule();
		rr.setRedex(new Bigraph());
		rr.getRedex().setSignature(SignatureXMLImport.importFile(sigFile));
		new ReactionRuleXMLExport().setModel(rr).setOutputStream(io.getOutputStream()).exportObject();
		sigFile.setContents(io.getInputStream(), 0, null);
	}
}
