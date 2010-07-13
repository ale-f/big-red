package dk.itu.big_red.model.import_export;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.util.DOM;

/**
 * XMLImport reads a XML document and produces a corresponding {@link Bigraph}.
 * @author alec
 * @see BigraphXMLExport
 *
 */
public class BigraphXMLImport extends Import<Bigraph> {
	@Override
	public Bigraph importModel() throws ImportFailedException {
		try {
			Document d = DOM.parse(source);
			return (Bigraph)process(d.getDocumentElement());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}
	
	private void processThing(Element e, Thing model) throws ImportFailedException {
		Element el = (Element)DOM.getNamedChildElement(e, "big-red:appearance");
		if (el != null) {
			AppearanceGenerator.setAppearance(el, model);
			el.getParentNode().removeChild(el);
		}
		
		for (int j = 0; j < e.getChildNodes().getLength(); j++) {
			if (!(e.getChildNodes().item(j) instanceof Element))
				continue;
			Object i = process((Element)e.getChildNodes().item(j));
			if (i instanceof ILayoutable)
				model.addChild((ILayoutable)i);
		}
	}
	
	private Object process(Element e) throws ImportFailedException {
		Object model = ModelFactory.getNewObject(e.getNodeName());
		if (model instanceof Thing) {
			processThing(e, (Thing)model);
		}
		return model;
	}
}
