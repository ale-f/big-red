package dk.itu.big_red.model.import_export;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ImportFailedException;
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
			Document d = DOM.parse(source);
			source.close();
			return (Signature)process(d.getDocumentElement());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}

	private void processControl(Element e, Control model) throws ImportFailedException {
		model.setLongName(DOM.getAttribute(e, "name"));
		
		Element el = DOM.removeNamedChildElement(e, "big-red:shape");
		if (el != null)
			AppearanceGenerator.setShape(el, model);
		
		el = DOM.removeNamedChildElement(e, "big-red:appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model);
		
		AppearanceGenerator.attributesToModel(e, model);
		
		for (int j = 0; j < e.getChildNodes().getLength(); j++) {
			if (!(e.getChildNodes().item(j) instanceof Element))
				continue;
			Object i = process((Element)e.getChildNodes().item(j));
			if (i instanceof Port)
				model.addPort((Port)i);
		}
	}
	
	private void processSignature(Element e, Signature model) throws ImportFailedException {
		for (int j = 0; j < e.getChildNodes().getLength(); j++) {
			if (!(e.getChildNodes().item(j) instanceof Element))
				continue;
			Object i = process((Element)e.getChildNodes().item(j));
			if (i instanceof Control)
				model.addControl((Control)i);
		}
	}
	
	private void processPort(Element e, Port model) {
		model.setName(DOM.getAttribute(e, "name"));
		
		Element el = DOM.removeNamedChildElement(e, "big-red:port-appearance");
		if (el != null) {
			model.setDistance(DOM.getDoubleAttribute(el, "distance"));
			model.setSegment(DOM.getIntAttribute(el, "segment"));
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
