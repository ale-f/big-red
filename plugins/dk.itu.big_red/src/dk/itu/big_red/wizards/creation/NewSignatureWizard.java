package dk.itu.big_red.wizards.creation;

import org.bigraph.model.Signature;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.resources.Project.ModificationRunner.Callback;
import dk.itu.big_red.utilities.ui.UI;

public class NewSignatureWizard extends Wizard implements INewWizard {
	private WizardNewFileCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
			Project.findContainerByPath(null, page.getContainerFullPath());
		if (c != null) {
			try {
				final IFile sigFile = c.getFile(new Path(page.getFileName()));
				IOAdapter io = new IOAdapter();
				SignatureXMLSaver r = new SignatureXMLSaver().setModel(new Signature());
				
				r.setFile(new EclipseFileWrapper(sigFile)).setOutputStream(io.getOutputStream()).
					exportObject();
				Project.setContents(sigFile, io.getInputStream(),
						new Callback() {
					@Override
					public void onSuccess() {
						try {
							UI.openInEditor(sigFile);
						} catch (PartInitException pie) {
							/* ? */
							pie.printStackTrace();
						}
					}
				});
				
				return true;
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
}
