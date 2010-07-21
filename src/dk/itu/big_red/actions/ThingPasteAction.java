package dk.itu.big_red.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.commands.ILayoutablePasteCommand;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.util.Utility;

public class ThingPasteAction extends SelectionAction {

	public ThingPasteAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	protected void init() {
		super.init();
		setText("Paste");
		setId(ActionFactory.PASTE.getId());
		
		setImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setHoverImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setDisabledImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		setEnabled(false);
	}
	
	@SuppressWarnings("unchecked")
	private Command createPasteCommand(List selectedObjects) {
		ILayoutable newParent = null;
		if (selectedObjects.size() == 0) {
			return null;
		} else if (selectedObjects.size() == 1) {
			Object i = selectedObjects.get(0);
			if (i instanceof EditPart && ((EditPart)i).getModel() instanceof
					ILayoutable)
				newParent = (ILayoutable)((EditPart)i).getModel();
			else return null;
		} else {
			Iterator<Object> it = selectedObjects.iterator();
			EditPart sharedParent = null;
			while (it.hasNext()) {
				Object i = it.next();
				if (!(i instanceof EditPart))
					continue;
				EditPart part = (EditPart)i;
				if (sharedParent == null) {
					sharedParent = part.getParent();
				} else if (sharedParent != part.getParent()) {
					return null;
				}
			}
			if (sharedParent.getModel() instanceof ILayoutable)
				newParent = (ILayoutable)sharedParent.getModel();
		}
		return new ILayoutablePasteCommand(newParent);
	}
	
	protected boolean calculateEnabled() {
		Command command = createPasteCommand(getSelectedObjects());
		return (command != null && command.canExecute());
	}
	
	public void run() {
		Command command = createPasteCommand(getSelectedObjects());
		if (command != null && command.canExecute())
			execute(command);
	}
}
