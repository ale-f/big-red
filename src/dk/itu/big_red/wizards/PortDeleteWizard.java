package dk.itu.big_red.wizards;

import java.util.ArrayList;

import dk.itu.big_red.GraphicalEditor;
import dk.itu.big_red.util.Utility;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Node;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class PortDeleteWizard extends SubWizard implements IPortSelector {
	protected String port;
	
	protected Bigraph getModel() {
		return ((GraphicalEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).getModel();
	}
	
	@Override
	public String getTitle() {
		return "Delete an existing port";
	}

	@Override
	public String getSummary() {
		return "Remove one of this bigraph's ports.";
	}

	@Override
	public Image getIcon() {
		return Utility.getImage(ISharedImages.IMG_TOOL_DELETE);
	}
	
	@Override
	public void init() {
		setWindowTitle(getTitle());
		
		ArrayList<WizardPage> pages = new ArrayList<WizardPage>();
		
		pages.add(new PortSelectionPage("Big Red.PortSelectWizard", this));
		
		for (WizardPage p : pages)
			addPage(p);
	}

	@Override
	public boolean performFinish() {
		MessageBox mb = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
		mb.setMessage("Are you sure you want to delete the port \"" + port + "\"?\n\n" +
				      "This action is not undoable!");
		mb.setText("Delete port?");
		int result = mb.open();
		
		if (result == SWT.YES) { 
			Bigraph b = getModel();
			
			b.getSignature().clearConnections(port);
			b.getSignature().connections.remove(port);
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getSelectedPort() {
		return port;
	}

	@Override
	public void setSelectedPort(String port) {
		this.port = port;
	}

}
