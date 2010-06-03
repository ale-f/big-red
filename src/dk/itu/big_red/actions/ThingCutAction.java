package dk.itu.big_red.actions;

import java.util.Iterator;
import java.util.List;



import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.commands.ThingCutCommand;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.util.Utility;

public class ThingCutAction extends SelectionAction {

	public ThingCutAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		setText("Cut");
		setId(ActionFactory.CUT.getId());
		
		setImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		setHoverImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		setDisabledImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
		setEnabled(false);
	}
	
	private Command createCutCommand(List<Object> selectedObjects) {
		if (selectedObjects == null || selectedObjects.isEmpty())
			return null;
		
		ThingCutCommand cmd = new ThingCutCommand();
		Iterator<Object> it = selectedObjects.iterator();
		while (it.hasNext()) {
			Thing node = (Thing)((EditPart)it.next()).getModel();
			if (!cmd.isCopyableNode(node))
				return null;
			else
				cmd.addElement(node);
		}
		return cmd;
	}
	
	@Override
	protected boolean calculateEnabled() {
		Command cmd = createCutCommand(getSelectedObjects());
		return cmd != null && cmd.canExecute();
	}
	
	@Override
	public void run() {
		Command cmd = createCutCommand(getSelectedObjects());
		if (cmd != null && cmd.canExecute())
			cmd.execute();
	}
}