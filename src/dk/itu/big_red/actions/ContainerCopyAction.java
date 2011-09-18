package dk.itu.big_red.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.commands.ILayoutableCopyCommand;
import dk.itu.big_red.util.UI;

public class ContainerCopyAction extends SelectionAction {

	public ContainerCopyAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		setText("Copy");
		setId(ActionFactory.COPY.getId());
		
		setImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setHoverImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setEnabled(false);
	}
	
	private Command createCopyCommand(List<Object> selectedObjects) {
		if (selectedObjects == null || selectedObjects.isEmpty())
			return null;
		
		ILayoutableCopyCommand cmd = new ILayoutableCopyCommand();
		for (Object i : selectedObjects) {
			if (!(i instanceof EditPart))
				continue;
			Object node = ((EditPart)i).getModel();
			if (!cmd.isCopyableNode(node))
				return null;
			else
				cmd.addElement(node);
		}
		return cmd;
	}

	@Override
	protected boolean calculateEnabled() {
		Command cmd = createCopyCommand(getSelectedObjects());
		return cmd != null && cmd.canExecute();
	}

	@Override
	public void run() {
		Command cmd = createCopyCommand(getSelectedObjects());
		if (cmd != null && cmd.canExecute())
			cmd.execute();
	}
}
