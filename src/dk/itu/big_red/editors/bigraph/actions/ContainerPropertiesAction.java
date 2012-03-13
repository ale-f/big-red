package dk.itu.big_red.editors.bigraph.actions;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.utilities.ui.UI;

public class ContainerPropertiesAction extends SelectionAction {
	public ContainerPropertiesAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(false);
	}
	
	@Override
	protected void init() {
		setText("&Properties...");
		setToolTipText("Properties");
		
		setId(ActionFactory.PROPERTIES.getId());
		
		ImageDescriptor icon =
			UI.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
		if (icon != null)
			setImageDescriptor(icon);
		setEnabled(false);
	}
	
	@Override
	protected boolean calculateEnabled() {
		/*	Why not? */
		return true;
	}
	
	@Override
	public void run() {
		try {
			UI.getWorkbenchPage().showView(IPageLayout.ID_PROP_SHEET);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}
}
