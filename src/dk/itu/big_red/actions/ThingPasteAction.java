package dk.itu.big_red.actions;

import java.util.Iterator;
import java.util.List;



import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.commands.ThingPasteCommand;
import dk.itu.big_red.model.Thing;
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
		Thing newParent = null;
		if (selectedObjects.size() == 0) {
			return null;
		} else if (selectedObjects.size() == 1) {
			newParent =
				(Thing)((EditPart)selectedObjects.get(0)).getModel();
		} else {
			Iterator<EditPart> it = selectedObjects.iterator();
			EditPart sharedParent = null;
			while (it.hasNext()) {
				EditPart part = it.next();
				if (sharedParent == null) {
					sharedParent = part.getParent();
				} else if (sharedParent != part.getParent()) {
					return null;
				}
			}
			newParent = (Thing)sharedParent.getModel();
		}
		return new ThingPasteCommand(newParent);
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
