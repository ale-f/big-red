package dk.itu.big_red.model.import_export;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.util.DOM;

public class SignatureXMLExport extends Export<Signature> {
	private Document doc = null;
	
	@Override
	public void exportObject() throws ExportFailedException {
		process(getModel());
		
		try {
			DOM.write(target, doc);
			target.close();
		} catch (Exception e) {
			throw new ExportFailedException(e);
		}
	}

	private void process(Signature s) {
		doc = DOM.createDocument(XMLNS.SIGNATURE, "signature");
		Element e = doc.getDocumentElement();
		DOM.applyAttributes(e,
			"xmlns:big-red", XMLNS.BIG_RED);

		for (Control c : s.getControls())
			DOM.appendChildIfNotNull(e, process(c));
	}
	
	private Element process(Control c) {
		Element e = doc.createElement("control");
		DOM.applyAttributes(e,
				"name", c.getLongName());
		
		for (Port p : c.getPortsArray())
			e.appendChild(process(p));
		
		DOM.appendChildIfNotNull(e, AppearanceGenerator.getShape(doc, c));
		DOM.appendChildIfNotNull(e, AppearanceGenerator.getAppearance(doc, c));
		AppearanceGenerator.modelToAttributes(e, c);
		
		return e;
	}
	
	private Element process(Port p) {
		Element e = doc.createElement("port");
		DOM.applyAttributes(e,
				"name", p.getName());
		
		Element pa = doc.createElement("big-red:port-appearance");
		DOM.applyAttributes(pa,
				"segment", p.getSegment(),
				"distance", p.getDistance());
		e.appendChild(pa);
		
		return e;
	}
}
