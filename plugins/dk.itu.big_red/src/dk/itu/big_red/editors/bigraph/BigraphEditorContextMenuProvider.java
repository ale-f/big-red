package dk.itu.big_red.editors.bigraph;

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
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;

public class BigraphEditorContextMenuProvider extends ContextMenuProvider {
	private ActionRegistry actionRegistry;
	
	public BigraphEditorContextMenuProvider(
			EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		setActionRegistry(registry);
	}
	
	private void populateMenu(
			final PaletteViewer pv, final PaletteContainer pc,
			final IMenuManager menu) {
		for (Object j : pc.getChildren()) {
			if (j instanceof ToolEntry) {
				final ToolEntry k = (ToolEntry)j;
				Action toolAction = new Action(k.getLabel()) {
					@Override
					public void run() {
						pv.setActiveTool(k);
					}
				};
				menu.add(toolAction);
			} else if (j instanceof PaletteContainer) {
				PaletteContainer k = (PaletteContainer)j;
				MenuManager sub = new MenuManager(k.getLabel());
				populateMenu(pv, k, sub);
				menu.add(sub);
			} else if (j instanceof PaletteSeparator) {
				menu.add(new Separator());
			}
		}
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
		
		menu.appendToGroup(GEFActionConstants.GROUP_REST,
				getActionRegistry().getAction(BigraphRelayoutAction.ID));
		
		menu.appendToGroup(GEFActionConstants.GROUP_REST,
				new Separator());
		
		MenuManager palette = new MenuManager("Palette");
		menu.appendToGroup(GEFActionConstants.GROUP_REST, palette);
		PaletteViewer pv = getViewer().getEditDomain().getPaletteViewer();
		populateMenu(getViewer().getEditDomain().getPaletteViewer(),
				pv.getPaletteRoot(), palette);
	}

	private ActionRegistry getActionRegistry() {
		return actionRegistry;
	}
	
	private void setActionRegistry(ActionRegistry registry) {
		actionRegistry = registry;
	}
}
