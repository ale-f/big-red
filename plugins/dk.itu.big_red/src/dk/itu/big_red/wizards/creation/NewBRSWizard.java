package dk.itu.big_red.wizards.creation;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.savers.BigraphXMLSaver;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.bigraph.model.savers.SimulationSpecXMLSaver;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.resources.Project.ModificationRunner;

public class NewBRSWizard extends Wizard implements INewWizard {
	private WizardNewProjectCreationPage page = null;
	
	@Override
	public boolean performFinish() {
		String projectName = page.getProjectName();
		IProject p = Project.getWorkspaceRoot().getProject(projectName);
		if (p.exists()) {
			page.setErrorMessage("A project with this name already exists.");
			return false;
		} else {
			try {
				IFolder
					signatures = p.getFolder("signatures"),
					agents = p.getFolder("agents"),
					rules = p.getFolder("rules");
				IFile
					signature = signatures.getFile(
							projectName + ".bigraph-signature"),
					agent = agents.getFile(
							projectName + ".bigraph-agent"),
					spec = p.getFile(
							projectName + ".bigraph-simulation-spec");
				IOAdapter
					big = new IOAdapter(),
					sig = new IOAdapter(),
					sim = new IOAdapter();
				
				Signature s = new Signature();
				FileData.setFile(s,
						new EclipseFileWrapper(signature));
				SignatureXMLSaver r = new SignatureXMLSaver().setModel(s);
				r.setFile(new EclipseFileWrapper(signature)).
					setOutputStream(sig.getOutputStream()).exportObject();
				
				Bigraph b = new Bigraph();
				b.setSignature(s);
				BigraphXMLSaver r1 = new BigraphXMLSaver().setModel(b);
				r1.setFile(new EclipseFileWrapper(agent)).
					setOutputStream(big.getOutputStream()).exportObject();
				SimulationSpecXMLSaver r2 = new SimulationSpecXMLSaver().setModel(new SimulationSpec());
				
				r2.setFile(new EclipseFileWrapper(spec)).setOutputStream(sim.getOutputStream()).
					exportObject();
				
				new ModificationRunner(null,
					new Project.CreateProject(p,
							Project.newBigraphProjectDescription(projectName)),
					new Project.OpenProject(p),
					new Project.CreateFolder(rules),
					new Project.CreateFolder(agents),
					new Project.CreateFolder(signatures),
					new Project.CreateFile(signature, sig.getInputStream()),
					new Project.CreateFile(agent, big.getInputStream()),
					new Project.CreateFile(spec, sim.getInputStream())).
						schedule();
				
				return true;
			} catch (SaveFailedException e) {
				page.setErrorMessage(e.getLocalizedMessage());
			}
			return false;
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		page = new WizardNewProjectCreationPage("newBRSWizardPage");
		
		page.setTitle("Bigraphical reactive system");
		page.setDescription("Create a new bigraphical reactive system.");
		
		addPage(page);
	}
}
