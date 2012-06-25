package dk.itu.big_red.wizards.creation;

import org.bigraph.model.Bigraph;
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
import dk.itu.big_red.model.load_save.savers.BigraphXMLSaver;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;
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
		IContainer c = page.getFolder();
		if (c != null) {
			try {
				IFile sigFile = page.getSignature();
				IFile bigFile = Project.getFile(c, page.getFileName());
				NewAgentWizard.createBigraph(sigFile, bigFile);
				UI.openInEditor(bigFile);
				return true;
			} catch (CoreException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (LoadFailedException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			} catch (SaveFailedException e) {
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

	protected static void createBigraph(IFile sigFile, final IFile bigFile)
			throws LoadFailedException, SaveFailedException, CoreException {
		final IOAdapter io = new IOAdapter();
		Bigraph b = new Bigraph();
		
		b.setSignature((Signature)Loader.fromFile(sigFile));
		new BigraphXMLSaver().setFile(bigFile).setModel(b).
			setOutputStream(io.getOutputStream()).exportObject();
		Project.setContents(bigFile, io.getInputStream(), null);
	}
}
