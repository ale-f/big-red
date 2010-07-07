package dk.itu.big_red.model.import_export;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import dk.itu.big_red.exceptions.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.util.DOM;

public class XMLImport extends Import {
	@Override
	public boolean canImport() {
		return (source != null);
	}

	@Override
	public Bigraph importModel() throws ImportFailedException {
		try {
			Document doc =
				DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
			
			return (Bigraph)process(doc.getDocumentElement());
		} catch (ParserConfigurationException e) {
			throw new ImportFailedException(e);
		} catch (SAXException e) {
			throw new ImportFailedException(e);
		} catch (IOException e) {
			throw new ImportFailedException(e);
		}
	}
	
	protected void processThing(Element e, Thing model) throws ImportFailedException {
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
	
	protected Object process(Element e) throws ImportFailedException {
		Object model = ModelFactory.getNewObject(e.getNodeName());
		if (model instanceof Thing) {
			processThing(e, (Thing)model);
		}
		return model;
	}
}
