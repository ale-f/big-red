package dk.itu.big_red.model.import_export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.util.DOM;

public class XMLExport extends Export {
	private Bigraph model = null;
	
	@Override
	public void setModel(Object model) {
		if (model instanceof Bigraph)
			this.model = (Bigraph)model;
	}

	@Override
	public boolean canExport() {
		return (this.model != null && this.target != null);
	}

	private Document doc = null;
	
	@Override
	public void exportModel() throws ExportFailedException {
		process((ILayoutable)model);
		
		TransformerFactory f = TransformerFactory.newInstance();
		Source source = new DOMSource(doc);
		Result result = new StreamResult(target);
		
		try {
			Transformer t = f.newTransformer();
			t.transform(source, result);
		} catch (TransformerException e) {
			throw new ExportFailedException(e);
		}
	}

	Element process(Bigraph obj) {
		DOMImplementation impl = DOM.getImplementation();
		doc = impl.createDocument(
				"http://pls.itu.dk/bigraphs/2010/bigraph", "bigraph", null);
		Element e = doc.getDocumentElement();
		DOM.applyAttributesToElement(e,
			"signature", "signatures/test.bigraph-signature",
			"xmlns:big-red", "http://pls.itu.dk/bigraphs/2010/big-red");
		return e;
	}
	
	Element process(Node n) {
		Element e = doc.createElement("node");
		e.setAttribute("control", n.getControl().getLongName());
		
		for (Port p : n.getPorts()) {
			org.w3c.dom.Node portE = process(p);
			if (portE != null)
				e.appendChild(portE);
		}
		
		return e;
	}
	
	Element process(Point p) {
		if (p.getConnections().size() != 0) {
			EdgeConnection connection = p.getConnections().get(0);
			Element e =
				doc.createElement(p.getClass().getSimpleName().toLowerCase());
			DOM.applyAttributesToElement(e,
					"name", p.getName(),
					"link", Integer.toString(connection.getParent().hashCode(), 16));
			return e;
		} else return null;
	}
	
	Element process(ILayoutable obj) {
		Element e = null;
		if (obj instanceof Bigraph) {
			e = process((Bigraph)obj);
		} else if (obj instanceof Node) {
			e = process((Node)obj);
		} else {
			e = doc.createElement(obj.getClass().getSimpleName().toLowerCase());
		}
		
		for (ILayoutable i : obj.getChildren())
			e.appendChild(process(i));
		
		Element appearanceElement =
			AppearanceGenerator.getAppearance(doc, obj);
		if (appearanceElement != null)
			e.appendChild(appearanceElement);
		
		return e;
	}
}
