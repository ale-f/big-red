package dk.itu.big_red.model.import_export;

import java.util.Collections;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.LinkConnection;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ClassComparator;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.model.interfaces.internal.INameable;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.HomogeneousIterable;

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
			throw new ExportFailedException("This document contains a node with no control.", ex);
		}
		e.setAttribute("name", n.getName());
		
		for (Port p : n.getPorts()) 
			DOM.appendChildIfNotNull(e, process(p));
		
		return e;
	}
	
	private Element process(Point p) {
		if (p.getConnections().size() != 0) {
			LinkConnection connection = p.getConnections().get(0);
			Element e =
				doc.createElement(p.getClass().getSimpleName().toLowerCase());
			DOM.applyAttributesToElement(e,
					"name", p.getName(),
					"link", connection.getTarget().getName());
			return e;
		} else return null;
	}
	
	private Element process(INameable e) {
		Element el = doc.createElement(e.getClass().getSimpleName().toLowerCase());
		DOM.applyAttributesToElement(el,
				"name", e.getName());
		return el;
	}
	
	/**
	 * Sorts the immediate children of the given {@link ILayoutable} into the
	 * order required by the {@code <bigraph>} schema.
	 * @deprecated Re-sorting the array every time you need to use it is
	 * inefficient (and randomly rearranging internal data structures is an
	 * awful idea, anyway). Use a group of {@link HomogeneousIterable}s
	 * instead.
	 * @param obj an ILayoutable
	 */
	@Deprecated
	public static void sortChildrenIntoSchemaOrder(ILayoutable obj) {
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
		} else return;
		Collections.sort(obj.getChildren(), comparator);
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
		
		sortChildrenIntoSchemaOrder(obj);
		
		for (ILayoutable i : obj.getChildren())
			e.appendChild(process(i));
		
		DOM.appendChildIfNotNull(e, AppearanceGenerator.getAppearance(doc, obj));
		
		return e;
	}
}
