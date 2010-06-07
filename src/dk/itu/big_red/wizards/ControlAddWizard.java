package dk.itu.big_red.wizards;

import java.util.ArrayList;

import dk.itu.big_red.util.Utility;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

public class ControlAddWizard extends SubWizard {
	private ControlEditorWizardPage editor = null;
	
	@Override
	public boolean performFinish() {
		editor.registerControlFromValues();
		return true;
	}
	
	@Override
	public String getTitle() {
		return "Add a new control";
	}

	@Override
	public String getSummary() {
		return "Add a new control to this bigraph.";
	}
	
	@Override
	public Image getIcon() {
		return Utility.getImage(ISharedImages.IMG_OBJ_ADD);
	}

	@Override
	public void init() {
		setWindowTitle(getTitle());
		
		ArrayList<WizardPage> pages = new ArrayList<WizardPage>(); 
		
		pages.add(editor = new ControlEditorWizardPage("Big Red.ControlEditorWizard"));
		
		for (WizardPage p : pages)
			addPage(p);
	}
}
