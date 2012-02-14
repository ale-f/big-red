package dk.itu.big_red.model.load_save.savers;

import org.w3c.dom.Element;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.XMLSaver;
import dk.itu.big_red.model.load_save.XMLNS;
import dk.itu.big_red.utilities.DOM;

public class SignatureXMLSaver extends XMLSaver {
	@Override
	public Signature getModel() {
		return (Signature)super.getModel();
	}
	
	@Override
	public SignatureXMLSaver setModel(ModelObject model) {
		if (model instanceof Signature)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(XMLSaver.createDocument(XMLNS.SIGNATURE, "signature:signature"));
		processObject(getDocumentElement(), getModel());
		finish();
	}

	@Override
	public Element processObject(Element e, Object s_) throws SaveFailedException {
		if (!(s_ instanceof Signature))
			throw new SaveFailedException(s_ + " isn't a Signature");
		Signature s = (Signature)s_;
		
		DOM.applyAttributes(e,
			"xmlns:big-red", XMLNS.BIG_RED,
			"xmlns:signature", XMLNS.SIGNATURE);

		for (Control c : s.getControls())
			appendChildIfNotNull(e,
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
		
		appendChildIfNotNull(e,
				AppearanceGenerator.getShape(getDocument(), c));
		appendChildIfNotNull(e,
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
}
