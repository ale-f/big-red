package dk.itu.big_red.model.import_export;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.SimulationSpec;

public class SimulationSpecXMLExport extends XMLExport<SimulationSpec> {

	@Override
	public void exportObject() throws ExportFailedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<?> getType() {
		return SimulationSpec.class;
	}

}
