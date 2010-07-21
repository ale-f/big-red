package dk.itu.big_red.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;

public class WizardAction extends Action {
	private IWorkbenchWindow window;
	private Class<? extends IWizard> wizardClass;
	
	public WizardAction(IWorkbenchWindow window, String id, String text, Class<? extends IWizard> wizardClass) {
		this.window = window;
		this.wizardClass = wizardClass;
		
		setId(id);
		setText(text);
	}
	
	@Override
	public void run() {
		IWizard wizard;
		try {
			wizard = wizardClass.newInstance();
			WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
			dialog.create();
			dialog.open();
		} catch (Exception e) {
		}
	}
}
