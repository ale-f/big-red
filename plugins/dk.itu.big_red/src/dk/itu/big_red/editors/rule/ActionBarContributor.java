package dk.itu.big_red.editors.rule;

import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import dk.itu.big_red.editors.assistants.RedActions;

public class ActionBarContributor extends
		dk.itu.big_red.editors.assistants.ActionBarContributor {
	@Override
	protected void buildActions() {
		super.buildActions();
		
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
		
		addRetargetAction(RedActions.createToggleGridAction());
		addRetargetAction(RedActions.createSnapNearAction());
		addRetargetAction(RedActions.createGuideDisplayAction());
		addRetargetAction(RedActions.createEdgeDisplayAction());
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
		toolBarManager.add(getAction(RedActions.ACTION_GUIDE));
		toolBarManager.add(getAction(RedActions.ACTION_EDGE));
	}

}
