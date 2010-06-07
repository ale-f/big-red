package dk.itu.big_red.wizards;

import java.util.ArrayList;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;

public class ControlsWizard extends Wizard {
	private ArrayList<SubWizard> wizards = new ArrayList<SubWizard>();
	
	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	public ControlsWizard() {
		wizards.add(new ControlAddWizard());
		wizards.add(new ControlModifyWizard());
		wizards.add(new ControlDeleteWizard());
//		wizards.add(new ControlImportWizard());
		
		for (SubWizard w : wizards)
			w.init();
		
		setWindowTitle("Controls");
		setForcePreviousAndNextButtons(true);
		
		ArrayList<WizardPage> pages = new ArrayList<WizardPage>();
		
		pages.add(
			new ActuallyUsefulWizardSelectionPage(
				"Big Red.ControlsWizardSelection",
				wizards, "Select", "Choose a control operation.",
				"&Select a control operation:"));
		
		for (WizardPage p : pages)
			addPage(p);
	}
	
}
