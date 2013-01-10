package it.uniud.bigredit;

import java.util.List;

import it.uniud.bigredit.editparts.NestedBigraphPart;
import it.uniud.bigredit.ipo.ISOLink;
import org.bigraph.model.Bigraph;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteSeparator;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;
import dk.itu.big_red.editors.bigraph.actions.BigraphRelayoutAction;

public class PlaygroundEditorContextMenuProvider extends ContextMenuProvider {
	private ActionRegistry actionRegistry;
	
	public PlaygroundEditorContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		setActionRegistry(registry);
	}
	
	private void populateMenu(final PaletteViewer pv, final PaletteContainer pc, final IMenuManager menu) {
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
			} else if (j instanceof PaletteContainer) {
				PaletteContainer k = (PaletteContainer)j;
				MenuManager sub = new MenuManager(k.getLabel());
				sub.add(new GroupMarker(GEFActionConstants.GROUP_REST));
				populateMenu(pv, k, sub);
				menu.appendToGroup(GEFActionConstants.GROUP_REST, sub);
			}
		}
	}
	
	@Override
	public void buildContextMenu(IMenuManager menu) {
		
		if(getViewer().getSelectedEditParts().size()==2){
			 if((getViewer().getSelectedEditParts().get(0) instanceof NestedBigraphPart)&&
					 (getViewer().getSelectedEditParts().get(1) instanceof NestedBigraphPart)){
				 ISOLink ipo= new ISOLink();
				 List<Bigraph> list=ipo.analyze(((NestedBigraphPart)getViewer().getSelectedEditParts().get(0)).getModel(),
						 ((NestedBigraphPart)getViewer().getSelectedEditParts().get(1)).getModel());

				 
			 }
		}
		
		
		
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
		
		final PaletteViewer pv = getViewer().getEditDomain().getPaletteViewer();
		if (pv != null) {
			for (Object i : pv.getPaletteRoot().getChildren()) {
				if (i instanceof PaletteContainer) {
					PaletteContainer pc = (PaletteContainer)i;
					
					Action menuAction = new Action() {
					};
					menuAction.setEnabled(false);
					menuAction.setText(pc.getLabel());
					menu.appendToGroup(GEFActionConstants.GROUP_REST, menuAction);
					
					populateMenu(pv, pc, menu);
				} else if (i instanceof PaletteSeparator) {
					menu.appendToGroup(GEFActionConstants.GROUP_REST, new Separator());
				}
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
