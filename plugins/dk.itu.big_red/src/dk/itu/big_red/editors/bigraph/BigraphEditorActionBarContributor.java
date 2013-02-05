package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.ui.actions.DeleteRetargetAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInRetargetAction;
import org.eclipse.gef.ui.actions.ZoomOutRetargetAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

import dk.itu.big_red.editors.assistants.ActionBarContributor;
import dk.itu.big_red.editors.assistants.RedActions;

public class BigraphEditorActionBarContributor extends ActionBarContributor {
	@Override
	protected void buildActions() {
		super.buildActions();
		
		IWorkbenchWindow iww = getPage().getWorkbenchWindow();
		
		addAction(ActionFactory.NEW.create(iww));
		addAction(ActionFactory.SAVE.create(iww));
		addAction(ActionFactory.PRINT.create(iww));
		
		addRetargetAction(new DeleteRetargetAction());

		addRetargetAction((RetargetAction)ActionFactory.CUT.create(iww));
		addRetargetAction((RetargetAction)ActionFactory.COPY.create(iww));
		addRetargetAction((RetargetAction)ActionFactory.PASTE.create(iww));
		
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
		
		addRetargetAction(RedActions.createToggleGridAction());
		addRetargetAction(RedActions.createSnapNearAction());
		addRetargetAction(RedActions.createGuideDisplayAction());
		addRetargetAction(RedActions.createEdgeDisplayAction());
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(getAction(ActionFactory.UNDO.getId()));
		toolBarManager.add(getAction(ActionFactory.REDO.getId()));
		toolBarManager.add(getAction(ActionFactory.DELETE.getId()));
		
		toolBarManager.add(new Separator());
		
		toolBarManager.add(getAction(ActionFactory.CUT.getId()));
		toolBarManager.add(getAction(ActionFactory.COPY.getId()));
		toolBarManager.add(getAction(ActionFactory.PASTE.getId()));
		
		toolBarManager.add(new Separator());
		
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
