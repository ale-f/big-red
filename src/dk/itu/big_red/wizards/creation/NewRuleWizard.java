package dk.itu.big_red.wizards.creation;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.import_export.ReactionRuleXMLExport;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;
import dk.itu.big_red.wizards.creation.assistants.WizardNewRuleCreationPage;


public class NewRuleWizard extends Wizard implements INewWizard {
	private WizardNewRuleCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
			Project.findContainerByPath(null, page.getFolderPath());
		if (c != null) {			
			try {
				IFile sigFile =
						Project.findFileByPath(null, page.getSignaturePath());
				IFile rrFile = Project.getFile(c, page.getFileName());
				NewRuleWizard.createReactionRule(sigFile, rrFile);
				UI.openInEditor(Project.getFile(c, page.getFileName()));
				return true;
			} catch (CoreException e) {
				e.printStackTrace();
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (ImportFailedException e) {
				e.printStackTrace();
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (ExportFailedException e) {
				e.printStackTrace();
				page.setErrorMessage(e.getLocalizedMessage());
			}
		}
		return false;
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new WizardNewRuleCreationPage("newRuleWizardPage", selection);
		
		page.setTitle("Rule");
		page.setDescription("Create a new rule in an existing bigraphical reactive system.");
		
		addPage(page);
	}

	public static void createReactionRule(IFile sigFile, IFile rrFile) throws ImportFailedException, ExportFailedException, CoreException {
		IOAdapter io = new IOAdapter();
		
		ReactionRule rr = new ReactionRule().setFile(rrFile);
		rr.setRedex(new Bigraph());
		rr.getRedex().setSignature((Signature)Import.fromFile(sigFile));
		
		new ReactionRuleXMLExport().setModel(rr).setOutputStream(io.getOutputStream()).exportObject();
		rrFile.setContents(io.getInputStream(), 0, null);
	}
}
