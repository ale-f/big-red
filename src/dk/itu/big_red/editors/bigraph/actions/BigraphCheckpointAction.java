package dk.itu.big_red.editors.bigraph.actions;

import java.util.List;

import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;

import dk.itu.big_red.editors.bigraph.parts.BigraphPart;
import dk.itu.big_red.util.UI;

public class BigraphCheckpointAction extends SelectionAction {
	public static final String ID =
			"dk.itu.big_red.editors.bigraph.actions.BigraphCheckpointAction";
	
	public BigraphCheckpointAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		setText("Chec&kpoint");
		setToolTipText("Checkpoint");
		
		setId(ID);
		
		ImageDescriptor icon =
			UI.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
		if (icon != null)
			setImageDescriptor(icon);
		setEnabled(false);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected boolean calculateEnabled() {
		List l = getSelectedObjects();
		if (l.size() != 1 || !(l.get(0) instanceof BigraphPart))
			return false;
		return true;
	}

	@Override
	public void run() {
		System.out.println("Checkpoint");
		System.out.println(((BigraphPart)getSelectedObjects().get(0)).getModel().checkpoint());
	}
}
