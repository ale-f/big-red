package example.big_red.interaction_manager.dummy;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import dk.itu.big_red.interaction_managers.InteractionManager;

public class DummyInteractionManager extends InteractionManager {
	@Override
	public void run(Shell parent) {
		MessageDialog.open(
				MessageDialog.INFORMATION, parent, "Dummy",
				"This is a Dummy Interaction Manager.\n" +
				"Dummy Interaction Manager description 3.\n\n" +
				getSimulationSpec(), 0);
	}
}
