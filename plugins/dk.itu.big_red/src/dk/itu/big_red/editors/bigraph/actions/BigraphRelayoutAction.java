package dk.itu.big_red.editors.bigraph.actions;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import dk.itu.big_red.editors.bigraph.commands.BigraphRelayoutCommand;
import dk.itu.big_red.editors.bigraph.parts.BigraphPart;
import dk.itu.big_red.utilities.ui.UI;

public class BigraphRelayoutAction extends SelectionAction {
	public static final String ID =
			"dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction";
	
	public BigraphRelayoutAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}
	
	@Override
	protected void init() {
		setText("&Relayout");
		setToolTipText("Relayout");
		
		setId(ID);
		
		ImageDescriptor icon =
			UI.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
		if (icon != null)
			setImageDescriptor(icon);
		setEnabled(false);
	}

	@SuppressWarnings("rawtypes")
	private static Command createCopyCommand(List selectedObjects) {
		if (selectedObjects == null || selectedObjects.isEmpty() ||
				selectedObjects.size() != 1)
			return null;
		
		Object i = selectedObjects.get(0);
		if (i instanceof BigraphPart) {
			BigraphRelayoutCommand cmd = new BigraphRelayoutCommand();
			cmd.setBigraph(((BigraphPart)i).getModel());
			cmd.prepare();
			return cmd;
		} else return null;
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
			execute(cmd);
	}
}
