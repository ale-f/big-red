package dk.itu.big_red.wizards.creation;

import org.bigraph.model.SimulationSpec;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.SimulationSpecXMLSaver;
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

public class NewSimulationSpecWizard extends Wizard implements INewWizard {
	private WizardNewFileCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
			Project.findContainerByPath(null, page.getContainerFullPath());
		if (c != null) {
			try {
				final IFile ssFile = c.getFile(new Path(page.getFileName()));
				IOAdapter io = new IOAdapter();
				SimulationSpecXMLSaver r = new SimulationSpecXMLSaver().
					setModel(new SimulationSpec());
				
				r.setFile(new EclipseFileWrapper(ssFile)).
					setOutputStream(io.getOutputStream()).exportObject();
				Project.setContents(ssFile, io.getInputStream(),
						new Callback() {
					@Override
					public void onSuccess() {
						try {
							UI.openInEditor(ssFile);
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
 		page = new WizardNewFileCreationPage("newSimulationSpecWizardPage", selection);
		
		page.setTitle("Simulation spec");
		page.setDescription("Create a simulation spec for an existing bigraphical reactive system.");
		page.setFileExtension("bigraph-simulation-spec");
		
		addPage(page);
	}
}
