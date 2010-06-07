package dk.itu.big_red.wizards;

import java.awt.Dialog;
import java.util.ArrayList;

import dk.itu.big_red.GraphicalEditor;
import dk.itu.big_red.util.Utility;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Node;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class ControlDeleteWizard extends SubWizard implements IControlSelector {
	protected Control control;
	
	protected Bigraph getModel() {
		return ((GraphicalEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).getModel();
	}
	
	@Override
	public boolean performFinish() {
		if (control.getLongName() == "Unknown") {
			WizardPage p = (WizardPage)getContainer().getCurrentPage();
			p.setErrorMessage("The default control is not deletable");
			
			return false;
		}
		MessageBox mb = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
		mb.setMessage("Are you sure you want to delete the control \"" + control.getLongName() + "\"?\n\n" +
				      "This action is not undoable!");
		mb.setText("Delete control?");
		int result = mb.open();
		
		if (result == SWT.YES) { 
			Bigraph b = getModel();
			b.getSignature().deleteControl(getSelectedControl());
			
			for (Thing n : b.findAllChildren(Node.class)) {
				Node node = (Node)n;
				if (node.getControl() == getSelectedControl())
					node.setControl(null);
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String getTitle() {
		return "Delete an existing control";
	}

	@Override
	public String getSummary() {
		return "Remove one of this bigraph's controls.";
	}
	
	@Override
	public Image getIcon() {
		return Utility.getImage(ISharedImages.IMG_TOOL_DELETE);
	}

	@Override
	public void init() {
		setWindowTitle(getTitle());
		
		ArrayList<WizardPage> pages = new ArrayList<WizardPage>();
		
		pages.add(new ControlSelectionPage("Big Red.ControlEditorWizard", this));
		
		for (WizardPage p : pages)
			addPage(p);
	}

	@Override
	public Control getSelectedControl() {
		return control;
	}

	@Override
	public void setSelectedControl(Control control) {
		this.control = control;
	}

	@Override
	public boolean canFinish() {
		return (this.control != null);
	}
}
