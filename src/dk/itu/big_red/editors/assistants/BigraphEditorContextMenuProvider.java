package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
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
		
		final PaletteViewer pv = getViewer().getEditDomain().getPaletteViewer();
		for (Object i :
			pv.getPaletteRoot().getChildren()) {
			if (i instanceof PaletteContainer) {
				PaletteContainer pc = (PaletteContainer)i;
				
				Action menuAction = new Action() {
				};
				menuAction.setEnabled(false);
				menuAction.setText(pc.getLabel());
				menu.appendToGroup(GEFActionConstants.GROUP_REST, menuAction);
				
				for (Object j : pc.getChildren()) {
					if (j instanceof ToolEntry) {
						final ToolEntry k = (ToolEntry)j;
						Action toolAction = new Action() {
							@Override
							public void run() {
								pv.setActiveTool(k);
							}
						};
						toolAction.setText(k.getLabel());
						menu.appendToGroup(GEFActionConstants.GROUP_REST, toolAction);
					}
				}
			} else if (i instanceof PaletteSeparator) {
				menu.appendToGroup(GEFActionConstants.GROUP_REST, new Separator());
			}
		}
	}

	private ActionRegistry getActionRegistry() {
		return actionRegistry;
	}
	
	private void setActionRegistry(ActionRegistry registry) {
		actionRegistry = registry;
	}
}
