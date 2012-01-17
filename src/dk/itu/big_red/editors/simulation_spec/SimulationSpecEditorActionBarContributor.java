package dk.itu.big_red.editors.simulation_spec;

import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

public class SimulationSpecEditorActionBarContributor extends
		ActionBarContributor {

	@Override
	protected void buildActions() {
		IWorkbenchWindow iww = getPage().getWorkbenchWindow();

		addAction(ActionFactory.UNDO.create(iww));
		addAction(ActionFactory.REDO.create(iww));
	}

	@Override
	protected void declareGlobalActionKeys() {
		// TODO Auto-generated method stub

	}

}
