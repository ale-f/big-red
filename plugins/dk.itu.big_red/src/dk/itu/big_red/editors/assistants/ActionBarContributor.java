package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ui.actions.RedoRetargetAction;
import org.eclipse.gef.ui.actions.UndoRetargetAction;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;

public abstract class ActionBarContributor
		extends org.eclipse.gef.ui.actions.ActionBarContributor {
	@Override
	protected void buildActions() {
		IWorkbenchWindow iww = getPage().getWorkbenchWindow();
		
		addRetargetAction(new UndoRetargetAction());
		addRetargetAction(new RedoRetargetAction());
		
		addRetargetAction((RetargetAction)ActionFactory.REVERT.create(iww));
		addRetargetAction((RetargetAction)ActionFactory.REFRESH.create(iww));
	}
	
	@Override
	protected void declareGlobalActionKeys() {
	}
}
