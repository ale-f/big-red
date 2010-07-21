package dk.itu.big_red.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.util.Utility;

public class ThingPropertiesAction extends SelectionAction {

	public ThingPropertiesAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(false);
	}
	
	protected void init() {
		setText("&Properties...");
		setToolTipText("Properties");
		
		setId(ActionFactory.PROPERTIES.getId());
		
		ImageDescriptor icon =
			Utility.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
		if (icon != null)
			setImageDescriptor(icon);
		setEnabled(false);
	}
	
	@Override
	protected boolean calculateEnabled() {
		/*	Why not? */
		return true;
	}
	
	public void run() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().
				getActivePage().showView(IPageLayout.ID_PROP_SHEET);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
