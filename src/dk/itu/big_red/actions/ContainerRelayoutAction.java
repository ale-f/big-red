package dk.itu.big_red.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;

import dk.itu.big_red.commands.ContainerRelayoutCommand;
import dk.itu.big_red.model.Container;
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
	
	private List<Object> lastObjects = null;
	private List<Command> lastCommands = null;
	
	private List<Command> createRelayoutCommands(List<Object> selectedObjects) {
		IEditorPart editor = getWorkbenchPart().getSite().getWorkbenchWindow().
				getActivePage().getActiveEditor();
		if (selectedObjects.equals(lastObjects))
			return lastCommands;
		
		ContainerRelayoutCommand command;
		lastObjects = selectedObjects;
		lastCommands = new ArrayList<Command>();
		for (Object i : selectedObjects) {
			if (i instanceof AbstractPart) {
				Object m = ((AbstractPart)i).getModel();
				if (m instanceof Container) {
					command = new ContainerRelayoutCommand();
					command.setEditor(editor);
					command.setModel(m);
					lastCommands.add(command);
				}
			}
		}
		
		return lastCommands;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected boolean calculateEnabled() {
		List<Command> commands = createRelayoutCommands(getSelectedObjects());
		for (Command i : commands)
			if (!i.canExecute())
				return false;
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		List<Command> commands = createRelayoutCommands(getSelectedObjects());
		for (Command i : commands)
			if (i.canExecute())
				execute(i);
	}
}