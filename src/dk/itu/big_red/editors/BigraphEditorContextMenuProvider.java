package dk.itu.big_red.editors;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

public class BigraphEditorContextMenuProvider extends ContextMenuProvider {
	private ActionRegistry actionRegistry;
	
	public BigraphEditorContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		setActionRegistry(registry);
	}
	
	@Override
	public void buildContextMenu(IMenuManager menu) {
		GEFActionConstants.addStandardActionGroups(menu);
		
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO,
				getActionRegistry().getAction(ActionFactory.UNDO.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_UNDO,
				getActionRegistry().getAction(ActionFactory.REDO.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_COPY,
				getActionRegistry().getAction(ActionFactory.CUT.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_COPY,
				getActionRegistry().getAction(ActionFactory.COPY.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_COPY,
				getActionRegistry().getAction(ActionFactory.PASTE.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
				getActionRegistry().getAction(ActionFactory.DELETE.getId()));
		
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT,
				getActionRegistry().getAction(ActionFactory.PROPERTIES.getId()));
	}

	private ActionRegistry getActionRegistry() {
		return actionRegistry;
	}
	
	private void setActionRegistry(ActionRegistry registry) {
		actionRegistry = registry;
	}
}
