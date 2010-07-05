package dk.itu.big_red.actions;

import java.util.Iterator;
import java.util.List;



import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.commands.ILayoutableCopyCommand;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.util.Utility;

public class ThingCopyAction extends SelectionAction {

	public ThingCopyAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	protected void init() {
		super.init();
		setText("Copy");
		setId(ActionFactory.COPY.getId());
		
		setImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setHoverImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setEnabled(false);
	}
	
	private Command createCopyCommand(List<Object> selectedObjects) {
		if (selectedObjects == null || selectedObjects.isEmpty())
			return null;
		
		ILayoutableCopyCommand cmd = new ILayoutableCopyCommand();
		Iterator<Object> it = selectedObjects.iterator();
		while (it.hasNext()) {
			Object i = it.next();
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
