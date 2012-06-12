package dk.itu.big_red.wizards.creation;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.Loader;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.savers.ReactionRuleXMLSaver;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;
import dk.itu.big_red.wizards.creation.assistants.WizardNewRuleCreationPage;

public class NewRuleWizard extends Wizard implements INewWizard {
	private WizardNewRuleCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c = page.getFolder();
		if (c != null) {			
			try {
				IFile sigFile = page.getSignature();
				IFile rrFile = Project.getFile(c, page.getFileName());
				NewRuleWizard.createReactionRule(sigFile, rrFile);
				UI.openInEditor(Project.getFile(c, page.getFileName()));
				return true;
			} catch (CoreException e) {
				e.printStackTrace();
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (LoadFailedException e) {
				e.printStackTrace();
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (SaveFailedException e) {
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

	protected static void createReactionRule(IFile sigFile, IFile rrFile)
			throws LoadFailedException, SaveFailedException, CoreException {
		IOAdapter io = new IOAdapter();
		
		ReactionRule rr = new ReactionRule();
		rr.setRedex(new Bigraph());
		rr.getRedex().setSignature((Signature)Loader.fromFile(sigFile));
		
		new ReactionRuleXMLSaver().setModel(rr).setFile(rrFile).
			setOutputStream(io.getOutputStream()).exportObject();
		rrFile.setContents(io.getInputStream(), 0, null);
	}
}
