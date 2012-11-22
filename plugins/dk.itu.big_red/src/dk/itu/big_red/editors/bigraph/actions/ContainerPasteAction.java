package dk.itu.big_red.editors.bigraph.actions;

import java.util.List;

import org.bigraph.model.Layoutable;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.editors.bigraph.commands.LayoutablePasteCommand;
import dk.itu.big_red.utilities.ui.UI;

public class ContainerPasteAction extends SelectionAction {
	public ContainerPasteAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		setText("Paste");
		setId(ActionFactory.PASTE.getId());
		
		setImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setHoverImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setDisabledImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		setEnabled(false);
	}
	
	@SuppressWarnings("rawtypes")
	private static Command createPasteCommand(List selectedObjects) {
		Layoutable newParent = null;
		if (selectedObjects.size() == 0) {
			return null;
		} else if (selectedObjects.size() == 1) {
			Object i = selectedObjects.get(0);
			if (i instanceof EditPart && ((EditPart)i).getModel() instanceof
					Layoutable)
				newParent = (Layoutable)((EditPart)i).getModel();
			else return null;
		} else {
			EditPart sharedParent = null;
			for (Object i : selectedObjects) {
				if (!(i instanceof EditPart))
					continue;
				EditPart part = (EditPart)i;
				if (sharedParent == null) {
					sharedParent = part.getParent();
				} else if (sharedParent != part.getParent()) {
					return null;
				}
			}
			if (sharedParent.getModel() instanceof Layoutable)
				newParent = (Layoutable)sharedParent.getModel();
		}
		LayoutablePasteCommand c = new LayoutablePasteCommand();
		c.setNewParent(newParent);
		c.prepare();
		return c;
	}
	
	@Override
	protected boolean calculateEnabled() {
		Command command = createPasteCommand(getSelectedObjects());
		return (command != null && command.canExecute());
	}
	
	@Override
	public void run() {
		Command command = createPasteCommand(getSelectedObjects());
		if (command != null && command.canExecute())
			execute(command);
	}
}
