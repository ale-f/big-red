package dk.itu.big_red.tools;

import dk.itu.big_red.utilities.ui.UI;

public class TestInteractionManager extends InteractionManager {

	@Override
	public void run() {
		UI.showMessageBox(0, "Gotcha", getSimulationSpec().toString());
	}

}
