package dk.itu.big_red.tools;

import dk.itu.big_red.editors.simulation_spec.SimulationSpecUIFactory;
import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.ui.UI;

public class BasicCommandLineInteractionManager extends InteractionManager {

	private Export<SimulationSpec> exporter;
	
	public BasicCommandLineInteractionManager(Export<SimulationSpec> exporter) {
		this.exporter = exporter;
	}
	
	@Override
	public void run() {
		try {
			IOAdapter io = new IOAdapter();
			exporter.setModel(getSimulationSpec()).
				setOutputStream(io.getOutputStream()).exportObject();
			SimulationSpecUIFactory.
				createResultsWindow(
					UI.getShell(), IOAdapter.readString(io.getInputStream())).
				open();
		} catch (ExportFailedException ex) {
			ex.printStackTrace();
		}
	}

}
