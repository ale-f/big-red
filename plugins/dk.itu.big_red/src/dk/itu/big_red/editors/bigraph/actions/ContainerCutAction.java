package dk.itu.big_red.editors.bigraph.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.editors.bigraph.commands.LayoutableCopyCommand;
import dk.itu.big_red.editors.bigraph.commands.LayoutableCutCommand;
import dk.itu.big_red.utilities.ui.UI;

public class ContainerCutAction extends SelectionAction {
	public ContainerCutAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		setText("Cut");
		setId(ActionFactory.CUT.getId());
		
		setImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		setHoverImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		setDisabledImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
		setEnabled(false);
	}
	
	@SuppressWarnings("rawtypes")
	private static Command createCutCommand(List selectedObjects) {
		if (selectedObjects == null || selectedObjects.isEmpty())
			return null;
		
		LayoutableCutCommand cmd = new LayoutableCutCommand();
		for (Object i : selectedObjects) {
			if (!(i instanceof EditPart))
				continue;
			Object node = ((EditPart)i).getModel();
			if (!LayoutableCopyCommand.canCopy(node))
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
			execute(cmd);
	}
}