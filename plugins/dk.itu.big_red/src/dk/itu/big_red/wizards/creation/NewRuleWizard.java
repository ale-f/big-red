package dk.itu.big_red.wizards.creation;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.savers.ReactionRuleXMLSaver;
import org.bigraph.model.savers.SaveFailedException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;

import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.resources.Project.ModificationRunner.Callback;
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
				final IFile rrFile = c.getFile(new Path(page.getFileName()));
				IOAdapter io = new IOAdapter();
				
				ReactionRule rr = new ReactionRule();
				rr.setRedex(new Bigraph());
				rr.getRedex().setSignature(
						NewAgentWizard.getSyntheticSignature(sigFile));
				ReactionRuleXMLSaver r = new ReactionRuleXMLSaver().setModel(rr);
				
				r.setFile(new EclipseFileWrapper(rrFile)).
					setOutputStream(io.getOutputStream()).exportObject();
				Project.setContents(rrFile, io.getInputStream(),
						new Callback() {
					@Override
					public void onSuccess() {
						try {
							UI.openInEditor(rrFile);
						} catch (PartInitException pie) {
							/* ? */
							pie.printStackTrace();
						}
					}
				});
				return true;
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
}
