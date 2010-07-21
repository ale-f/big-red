package dk.itu.big_red.actions;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;

import dk.itu.big_red.commands.ThingRelayoutCommand;
import dk.itu.big_red.part.AbstractPart;
import dk.itu.big_red.util.Utility;

public class ThingRelayoutAction extends SelectionAction {

	public ThingRelayoutAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}
	
	protected void init() {
		setText("&Relayout");
		setToolTipText("Relayout");
		
		setId("net.ybother.big_red.relayout");
		
		ImageDescriptor icon =
			Utility.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
		if (icon != null)
			setImageDescriptor(icon);
	}
	
	private Command createRelayoutCommand(List<Object> selectedObjects) {
		if (selectedObjects == null || selectedObjects.size() != 1)
			return null;
		
		ThingRelayoutCommand cmd = new ThingRelayoutCommand();
		cmd.setEditor(getWorkbenchPart().getSite().getWorkbenchWindow().getActivePage().getActiveEditor());
		
		Object model = selectedObjects.get(0);
		if (model instanceof AbstractPart)
			cmd.setModel(((AbstractPart)model).getModel());
		
		return cmd;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean calculateEnabled() {
		Command cmd = createRelayoutCommand(getSelectedObjects());
		return cmd != null && cmd.canExecute();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Command cmd = createRelayoutCommand(getSelectedObjects());
		if (cmd != null && cmd.canExecute())
			cmd.execute();
	}
}