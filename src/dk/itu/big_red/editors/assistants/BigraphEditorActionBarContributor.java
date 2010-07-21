package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ui.actions.*;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

public class BigraphEditorActionBarContributor extends ActionBarContributor {

	@Override
	protected void buildActions() {
		IWorkbenchWindow iww = getPage().getWorkbenchWindow();
		
		addAction(ActionFactory.NEW.create(iww));
		addAction(ActionFactory.SAVE.create(iww));
		addAction(ActionFactory.PRINT.create(iww));
		
		addRetargetAction((RetargetAction)ActionFactory.REVERT.create(iww));
		
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		addRetargetAction(new DeleteRetargetAction());

		addRetargetAction((RetargetAction)ActionFactory.CUT.create(iww));
		addRetargetAction((RetargetAction)ActionFactory.COPY.create(iww));
		addRetargetAction((RetargetAction)ActionFactory.PASTE.create(iww));
		
		addRetargetAction(new ZoomInRetargetAction());
		addRetargetAction(new ZoomOutRetargetAction());
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(getAction(ActionFactory.NEW.getId()));
		toolBarManager.add(getAction(ActionFactory.SAVE.getId()));
		toolBarManager.add(getAction(ActionFactory.PRINT.getId()));
		
		toolBarManager.add(new Separator());
		
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
	}

	public void contributeToMenu(IMenuManager menuManager) {
	}
	
	@Override
	protected void declareGlobalActionKeys() {
		// TODO Auto-generated method stub
		
	}

}
