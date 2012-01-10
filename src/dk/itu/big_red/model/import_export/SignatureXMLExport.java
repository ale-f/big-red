package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.utilities.DOM;

public class SignatureXMLExport extends XMLExport<Signature> {
	@Override
	public void exportObject() throws ExportFailedException {
		setDocument(DOM.createDocument(XMLNS.SIGNATURE, "signature:signature"));
		processSignature(getDocumentElement(), getModel());
		finish();
	}

	public Element processSignature(Element e, Signature s) {
		DOM.applyAttributes(e,
			"xmlns:big-red", XMLNS.BIG_RED,
			"xmlns:signature", XMLNS.SIGNATURE);

		for (Control c : s.getControls())
			DOM.appendChildIfNotNull(e,
				processControl(newElement(XMLNS.SIGNATURE, "signature:control"), c));
		return e;
	}
	
	private Element processControl(Element e, Control c) {
		DOM.applyAttributes(e,
				"name", c.getName(),
				"kind", c.getKind().toString());
		
		for (Port p : c.createPorts())
			e.appendChild(processPort(
				newElement(XMLNS.SIGNATURE, "signature:port"), p));
		
		DOM.appendChildIfNotNull(e,
				AppearanceGenerator.getShape(getDocument(), c));
		DOM.appendChildIfNotNull(e,
				AppearanceGenerator.getAppearance(getDocument(), c));
		AppearanceGenerator.modelToAttributes(e, c);
		
		return e;
	}
	
	private Element processPort(Element e, Port p) {
		DOM.applyAttributes(e,
				"name", p.getName());
		
		e.appendChild(
			DOM.applyAttributes(
				newElement(XMLNS.BIG_RED, "big-red:port-appearance"),
				"segment", p.getSegment(),
				"distance", p.getDistance()));
		
		return e;
	}
	
	@Override
	public Class<?> getType() {
		return Signature.class;
	}
}
