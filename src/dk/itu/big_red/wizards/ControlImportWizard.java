package dk.itu.big_red.wizards;

import dk.itu.big_red.util.Utility;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;

public class ControlImportWizard extends SubWizard {

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTitle() {
		return "Import controls from an existing bigraph";
	}

	@Override
	public String getSummary() {
		return "Import controls from an existing bigraph.";
	}
	
	@Override
	public Image getIcon() {
		return Utility.getImage(ISharedImages.IMG_OBJ_FILE);
	}

	@Override
	public void init() {
	}

}
