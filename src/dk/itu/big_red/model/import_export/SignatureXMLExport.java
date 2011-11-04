package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.util.DOM;

public class SignatureXMLExport extends XMLExport<Signature> {
	@Override
	public void exportObject() throws ExportFailedException {
		process(getModel());
		finish();
	}

	private void process(Signature s) {
		setDocument(DOM.createDocument(XMLNS.SIGNATURE, "signature"));
		Element e = getDocument().getDocumentElement();
		DOM.applyAttributes(e,
			"xmlns:big-red", XMLNS.BIG_RED);

		for (Control c : s.getControls())
			DOM.appendChildIfNotNull(e, process(c));
	}
	
	private Element process(Control c) {
		Element e = newElement(XMLNS.SIGNATURE, "control");
		DOM.applyAttributes(e,
				"name", c.getLongName());
		
		for (Port p : c.getPortsArray())
			e.appendChild(process(p));
		
		DOM.appendChildIfNotNull(e,
				AppearanceGenerator.getShape(getDocument(), c));
		DOM.appendChildIfNotNull(e,
				AppearanceGenerator.getAppearance(getDocument(), c));
		AppearanceGenerator.modelToAttributes(e, c);
		
		return e;
	}
	
	private Element process(Port p) {
		Element e = newElement(XMLNS.SIGNATURE, "port");
		DOM.applyAttributes(e,
				"name", p.getName());
		
		Element pa = newElement(XMLNS.BIG_RED, "big-red:port-appearance");
		DOM.applyAttributes(pa,
				"segment", p.getSegment(),
				"distance", p.getDistance());
		e.appendChild(pa);
		
		return e;
	}
}
