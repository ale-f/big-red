package dk.itu.big_red.model.import_export;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.model.interfaces.internal.INameable;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.Utility;

/**
 * XMLExport writes a {@link Bigraph} out as an XML document.
 * @author alec
 * @see BigraphXMLImport
 *
 */
public class BigraphXMLExport extends ModelExport<Bigraph> {
	/**
	 * An array of model {@link Class}es in the appropriate order for the
	 * <code>&lt;bigraph&gt;</code> XML schema, suitable for giving as the
	 * last argument to {@link Utility#groupListByClass(java.util.List,
	 * Object...)}.
	 */
	public static final Object SCHEMA_ORDER[] = {
		Edge.class, OuterName.class, Root.class, InnerName.class,
		Port.class, Node.class, Site.class
	};
	
	private Document doc = null;
	
	@Override
	public void exportObject() throws ExportFailedException {
		try {
			process((ILayoutable)model);
			DOM.write(target, doc);
			target.close();
		} catch (ExportFailedException e) {
			throw e;
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
	
	private Element process(Node n) throws ExportFailedException {
		Element e = doc.createElement("node");
		try {
			e.setAttribute("control", n.getControl().getLongName());
		} catch (NullPointerException ex) {
			throw new ExportFailedException("Node \"" + n.getName() + "\" has no control.", ex);
		}
		e.setAttribute("name", n.getName());
		
		for (Port p : n.getPorts()) 
			DOM.appendChildIfNotNull(e, process(p));
		
		return e;
	}
	
	private Element process(Point p) throws ExportFailedException {
		Link link = p.getLink();
		if (link != null) {
			Element e =
				doc.createElement(p.getClass().getSimpleName().toLowerCase());
			DOM.applyAttributesToElement(e,
					"name", p.getName(),
					"link", link.getName());
			return e;
		} else if (p instanceof InnerName) {
			throw new ExportFailedException("Inner name \"" + p.getName() + "\" isn't connected to anything.");
		}
		return null;
	}
	
	private Element process(INameable e) {
		Element el = doc.createElement(e.getClass().getSimpleName().toLowerCase());
		DOM.applyAttributesToElement(el,
				"name", e.getName());
		return el;
	}
	
	private Element process(ILayoutable obj) throws ExportFailedException {
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
		
		if (obj instanceof Container) {
			Container c = (Container)obj;
			for (ILayoutable i : Utility.groupListByClass(c.getChildren(),
					BigraphXMLExport.SCHEMA_ORDER))
				e.appendChild(process(i));
		}
		
		DOM.appendChildIfNotNull(e, AppearanceGenerator.getAppearance(doc, obj));
		
		return e;
	}
}
