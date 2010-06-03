package dk.itu.big_red.wizards;

import java.util.ArrayList;

import dk.itu.big_red.model.Control;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbench;

public class ControlModifyWizard extends SubWizard implements IControlSelector {
	private Control control = null;
	private ControlEditorWizardPage editor = null;
	
	@Override
	public boolean performFinish() {
		editor.registerControlFromValues();
		return true;
	}

	@Override
	public String getTitle() {
		return "Modify an existing control";
	}

	@Override
	public String getSummary() {
		return "Modify one of this bigraph's existing controls.";
	}
	
	@Override
	public Image getIcon() {
		return null;
	}

	@Override
	public void init() {
		setWindowTitle(getTitle());
		
		ArrayList<WizardPage> pages = new ArrayList<WizardPage>(); 
		
		pages.add(new ControlSelectionPage("Big Red.ControlSelectWizard", this));
		pages.add(editor = new ControlEditorWizardPage("Big Red.ControlEditorWizard"));
		
		for (WizardPage p : pages)
			addPage(p);
	}

	@Override
	public void setSelectedControl(Control control) {
		this.control = control;
		editor.updateFromControl();
	}

	@Override
	public Control getSelectedControl() {
		return control;
	}

}
