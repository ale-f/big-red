package dk.itu.big_red.actions;

import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;

import dk.itu.big_red.commands.ContainerRelayoutCommand;
import dk.itu.big_red.part.AbstractPart;
import dk.itu.big_red.util.Utility;

public class ContainerRelayoutAction extends SelectionAction {
	public static final String ID = "dk.itu.big_red.relayout";
	
	public ContainerRelayoutAction(IWorkbenchPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}
	
	@Override
	protected void init() {
		super.init();
		
		setText("&Relayout");
		setToolTipText("Relayout");
		
		setId(ID);
		
		ImageDescriptor icon =
			Utility.getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
		if (icon != null)
			setImageDescriptor(icon);
	}
	
	private Command createRelayoutCommand(List<Object> selectedObjects) {
		if (selectedObjects == null || selectedObjects.size() != 1)
			return null;
		
		ContainerRelayoutCommand command = new ContainerRelayoutCommand();
		command.setEditor(getWorkbenchPart().getSite().getWorkbenchWindow().getActivePage().getActiveEditor());
		
		Object model = selectedObjects.get(0);
		if (model instanceof AbstractPart)
			command.setModel(((AbstractPart)model).getModel());
		
		return command;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean calculateEnabled() {
		Command command = createRelayoutCommand(getSelectedObjects());
		return command != null && command.canExecute();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		Command command = createRelayoutCommand(getSelectedObjects());
		if (command != null && command.canExecute())
			execute(command);
	}
}