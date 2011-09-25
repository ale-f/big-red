package dk.itu.big_red.model.import_export;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.util.DOM;

public class SignatureXMLImport extends ModelImport<Signature> {

	@Override
	public Signature importObject() throws ImportFailedException {
		try {
			Document d =
				DOM.validate(DOM.parse(source), RedPlugin.getPluginResource("schema/signature.xsd"));
			source.close();
			return (Signature)process(d.getDocumentElement());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}

	private void processControl(Element e, Control model) throws ImportFailedException {
		model.setLongName(DOM.getAttributeNS(e, XMLNS.SIGNATURE, "name"));
		
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "shape");
		if (el != null)
			AppearanceGenerator.setShape(el, model);
		
		el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model);
		
		AppearanceGenerator.attributesToModel(e, model);
		
		for (Element j : DOM.getChildElements(e)) {
			Object i = process(j);
			if (i instanceof Port)
				model.addPort((Port)i);
		}
	}
	
	private void processSignature(Element e, Signature model) throws ImportFailedException {
		for (Element j : DOM.getChildElements(e)) {
			Object i = process(j);
			if (i instanceof Control)
				model.addControl((Control)i);
		}
	}
	
	private void processPort(Element e, Port model) {
		model.setName(DOM.getAttributeNS(e, XMLNS.SIGNATURE, "name"));
		
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "port-appearance");
		if (el != null) {
			model.setDistance(DOM.getDoubleAttribute(el, XMLNS.BIG_RED, "distance"));
			model.setSegment(DOM.getIntAttribute(el, XMLNS.BIG_RED, "segment"));
		}
	}
	
	private Object process(Element e) throws ImportFailedException {
		Object model = ModelFactory.getNewObject(e.getTagName());
		if (model instanceof Signature)
			processSignature(e, (Signature)model);
		else if (model instanceof Control)
			processControl(e, (Control)model);
		else if (model instanceof Port)
			processPort(e, (Port)model);
		return model;
	}
}
