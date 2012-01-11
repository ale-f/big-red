package dk.itu.big_red.model.import_export;

import java.io.IOException;
import java.io.OutputStreamWriter;

import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;

public class SimulationSpecBigMCExport extends Export<SimulationSpec> {
	private OutputStreamWriter osw = null;
	private int indentation = -1;
	
	private void newline() throws ExportFailedException {
		write("\n");
		for (int i = 0; i < indentation; i++)
			write("\t");
	}
	
	private void write(String str) throws ExportFailedException {
		try {
			osw.write(str);
		} catch (IOException e) {
			throw new ExportFailedException(e);
		}
	}
	
	@Override
	public void exportObject() throws ExportFailedException {
		osw = new OutputStreamWriter(getOutputStream());
		processSimulationSpec(getModel());
		try {
			osw.close();
		} catch (IOException e) {
			throw new ExportFailedException(e);
		}
	}

	private void processSignature(Signature s) throws ExportFailedException {
		write("# Controls"); newline();
		for (Control c : s.getControls()) {
			switch (c.getKind()) {
			case ACTIVE:
			case ATOMIC:
			default:
				write("%active ");
				break;
			case PASSIVE:
				write("%passive ");
				break;
			}
			write(c.getName() + " : ");
			write(c.getPorts().size() + ";"); newline();
		}
		newline();
	}
	
	private void processSimulationSpec(SimulationSpec s) throws ExportFailedException {
		processSignature(s.getSignature());
	}
}
