package dk.itu.big_red.wizards;

import dk.itu.big_red.util.Utility;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

public class PortImportWizard extends SubWizard {

	@Override
	public String getTitle() {
		return "Import ports from an existing bigraph";
	}

	@Override
	public String getSummary() {
		return "Import ports from an existing bigraph.";
	}

	@Override
	public Image getIcon() {
		return Utility.getImage(ISharedImages.IMG_OBJ_FILE);
	}
	
	@Override
	public void init() {
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

}
