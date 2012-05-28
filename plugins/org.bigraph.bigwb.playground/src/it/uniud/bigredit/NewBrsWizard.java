package it.uniud.bigredit;

import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.load_save.savers.BRSXMLSaver;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.Loader;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;



public class NewBrsWizard  extends Wizard implements INewWizard {
	private NewBrsWizardPage page = null;
	
	@Override
	public boolean performFinish() {
		IContainer c =
				Project.findContainerByPath(null, page.getFolderPath());
			if (c != null) {
				try {
					IFile sigFile =
						Project.findFileByPath(null, page.getSignaturePath());
					IFile bigFile = Project.getFile(c, page.getFileName());
					NewBrsWizard.createBigraph(sigFile, bigFile);
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
		page = new NewBrsWizardPage("newBrsWizardPage", selection);
		
		page.setTitle("BRS");
		page.setDescription("Create a new brs");
		
		addPage(page);
	}
	

	protected static void createBigraph(IFile sigFile, IFile bigFile)
			throws LoadFailedException, SaveFailedException, CoreException {
		IOAdapter io = new IOAdapter();
		BRS b = new BRS();
		
		b.setSignature((Signature)Loader.fromFile(sigFile));
		new BRSXMLSaver().setFile(bigFile).setModel(b).
			setOutputStream(io.getOutputStream()).exportObject();
		bigFile.setContents(io.getInputStream(), 0, null);
	}
	
	
	public static IFile getFile(IContainer c, String name) throws CoreException {
		IFile f = c.getFile(new Path(name));
		if (!f.exists())
			f.create(IOAdapter.getNullInputStream(), true, null);
		return f;
	}
	
	public static IResource findResourceByPath(IContainer c, IPath path) {
		if (c == null)
			c = getWorkspaceRoot();
		return c.findMember(path);
	}
	
	public static IContainer findContainerByPath(IContainer c, IPath path) {
		IResource r = findResourceByPath(c, path);
		return (r instanceof IContainer ? (IContainer)r : null);
	}
	
	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	
	
}
