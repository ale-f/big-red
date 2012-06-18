package dk.itu.big_red.editors.rule;

import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.RetargetAction;

import dk.itu.big_red.application.plugin.RedPlugin;

public class ActionBarContributor extends
		dk.itu.big_red.editors.assistants.ActionBarContributor {
	@Override
	protected void buildActions() {
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
		
		addRetargetAction(new RetargetAction(
				GEFActionConstants.TOGGLE_GRID_VISIBILITY,
				"Snap to grid", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(RedPlugin.getImageDescriptor(
						"resources/icons/actions/snap-to-grid.png"));
			}
		});
		addRetargetAction(new RetargetAction(
				GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY,
				"Snap to nearby objects", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(RedPlugin.getImageDescriptor(
						"resources/icons/actions/snap-to-object.png"));
			}
		});
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(getAction(GEFActionConstants.ZOOM_IN));
		toolBarManager.add(getAction(GEFActionConstants.ZOOM_OUT));
		toolBarManager.add(new ZoomComboContributionItem(getPage()));
		
		toolBarManager.add(new Separator());
		
		toolBarManager.add(
				getAction(GEFActionConstants.TOGGLE_GRID_VISIBILITY));
		toolBarManager.add(
				getAction(GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY));
	}

}
