package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.SimulationSpec;

public class SimulationSpecXMLImport extends Import<SimulationSpec> {

	@Override
	public SimulationSpec importObject() throws ImportFailedException {
		return new SimulationSpec();
	}

	public static SimulationSpec importFile(IFile file) throws ImportFailedException {
		SimulationSpecXMLImport s = new SimulationSpecXMLImport();
		try {
			s.setInputStream(file.getContents());
			return s.importObject().setFile(file);
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}
}
