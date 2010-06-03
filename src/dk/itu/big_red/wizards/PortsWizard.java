package dk.itu.big_red.wizards;

import java.util.ArrayList;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IWorkbench;

public class PortsWizard extends Wizard {
	private ArrayList<SubWizard> wizards = new ArrayList<SubWizard>();
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	public PortsWizard() {
		wizards.add(new PortAddWizard());
		wizards.add(new PortModifyWizard());
		wizards.add(new PortDeleteWizard());
//		wizards.add(new PortImportWizard());
		
		for (SubWizard w : wizards)
			w.init();
		
		setWindowTitle("Ports");
		setForcePreviousAndNextButtons(true);
		
		ArrayList<WizardPage> pages = new ArrayList<WizardPage>();
		
		pages.add(
				new ActuallyUsefulWizardSelectionPage(
					"Big Red.PortsWizardSelection",
					wizards, "Select", "Choose a port operation.",
					"&Select a port operation:"));
		
		for (WizardPage p : pages)
			addPage(p);
	}
	
}