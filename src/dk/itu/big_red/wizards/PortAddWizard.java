package dk.itu.big_red.wizards;

import java.util.ArrayList;

import dk.itu.big_red.util.Utility;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

public class PortAddWizard extends SubWizard implements IPortSelector {
	private String port = null;
	private PortEditorWizardPage editor = null;
	
	@Override
	public boolean performFinish() {
		editor.registerPortFromValues();
		return true;
	}
	
	@Override
	public String getTitle() {
		return "Add a new port";
	}

	@Override
	public String getSummary() {
		return "Add a new port to this bigraph.";
	}
	
	@Override
	public Image getIcon() {
		return Utility.getImage(ISharedImages.IMG_OBJ_ADD);
	}
	
	@Override
	public void init() {
		setWindowTitle(getTitle());
		
		ArrayList<WizardPage> pages = new ArrayList<WizardPage>(); 
		
		pages.add(editor = new PortEditorWizardPage("Big Red.PortEditorWizard"));
		
		for (WizardPage p : pages)
			addPage(p);
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
