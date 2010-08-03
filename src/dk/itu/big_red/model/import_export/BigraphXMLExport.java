package dk.itu.big_red.model.import_export;

import java.util.ArrayList;
import java.util.Collections;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ClassComparator;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.INameable;
import dk.itu.big_red.util.DOM;

/**
 * XMLExport writes a {@link Bigraph} out as an XML document.
 * @author alec
 * @see BigraphXMLImport
 *
 */
public class BigraphXMLExport extends ModelExport<Bigraph> {
	private Document doc = null;
	
	@Override
	public void exportObject() throws ExportFailedException {
		process((ILayoutable)model);
		
		try {
			DOM.write(target, doc);
			target.close();
		} catch (Exception e) {
			throw new ExportFailedException(e);
		}
	}

	private Element process(Bigraph obj) {
		DOMImplementation impl = DOM.getImplementation();
		doc = impl.createDocument(
				XMLNS.BIGRAPH, "bigraph", null);
		Element e = doc.getDocumentElement();
		DOM.applyAttributesToElement(e,
			"signature", obj.getSignatureFile().getFullPath().makeRelative().toString(),
			"xmlns:big-red", XMLNS.BIG_RED);
		return e;
	}
	
	private Element process(Node n) {
		Element e = doc.createElement("node");
		e.setAttribute("control", n.getControl().getLongName());
		e.setAttribute("name", n.getName());
		
		for (Port p : n.getPorts()) 
			DOM.appendChildIfNotNull(e, process(p));
		
		return e;
	}
	
	private Element process(Point p) {
		if (p.getConnections().size() != 0) {
			EdgeConnection connection = p.getConnections().get(0);
			Element e =
				doc.createElement(p.getClass().getSimpleName().toLowerCase());
			DOM.applyAttributesToElement(e,
					"name", p.getName(),
					"link", connection.getParent().getName());
			return e;
		} else return null;
	}
	
	private Element process(INameable e) {
		Element el = doc.createElement(e.getClass().getSimpleName().toLowerCase());
		DOM.applyAttributesToElement(el,
				"name", e.getName());
		return el;
	}
	
	private Element process(ILayoutable obj) {
		Element e = null;
		if (obj instanceof Bigraph) {
			e = process((Bigraph)obj);
		} else if (obj instanceof Node) {
			e = process((Node)obj);
		} else if (obj instanceof Point) {
			e = process((Point)obj);
		} else if (obj instanceof INameable) {
			e = process((INameable)obj);
		} else {
			e = doc.createElement(obj.getClass().getSimpleName().toLowerCase());
		}
		
		ClassComparator<ILayoutable> comparator =
			new ClassComparator<ILayoutable>();
		if (obj instanceof Bigraph) {
			comparator.setClassOrder(
					Edge.class,
					OuterName.class,
					Root.class,
					InnerName.class);
		} else if (obj instanceof Root) {
			comparator.setClassOrder(
					Node.class,
					Site.class);
		} else if (obj instanceof Node) {
			comparator.setClassOrder(
					Port.class,
					Node.class,
					Site.class);
		}
		
		ArrayList<ILayoutable> children =
			new ArrayList<ILayoutable>(obj.getChildren());
		Collections.sort(children, comparator);
		
		for (ILayoutable i : children)
			e.appendChild(process(i));
		
		DOM.appendChildIfNotNull(e, AppearanceGenerator.getAppearance(doc, obj));
		
		return e;
	}
}
