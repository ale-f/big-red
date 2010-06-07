package dk.itu.big_red.wizards;

import java.util.ArrayList;

import org.eclipse.jface.wizard.WizardPage;

public class PortModifyWizard extends SubWizard implements IPortSelector {
	private String port = null;
	private PortEditorWizardPage editor = null;
	
	@Override
	public boolean performFinish() {
		editor.registerPortFromValues();
		return true;
	}

	
	@Override
	public String getTitle() {
		return "Modify an existing port";
	}

	@Override
	public String getSummary() {
		return "Modify one of this bigraph's existing ports.";
	}

	@Override
	public void init() {
		setWindowTitle(getTitle());
		
		ArrayList<WizardPage> pages = new ArrayList<WizardPage>(); 
		
		pages.add(new PortSelectionPage("Big Red.PortSelectWizard", this));
		pages.add(editor = new PortEditorWizardPage("Big Red.PortEditorWizard"));
		
		for (WizardPage p : pages)
			addPage(p);
	}

	@Override
	public String getSelectedPort() {
		return this.port;
	}

	@Override
	public void setSelectedPort(String port) {
		this.port = port;
		editor.updateFromPort();
	}

}
