package org.bigraph.model.savers;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;
import static org.bigraph.model.loaders.RedNamespaceConstants.SIGNATURE;

/**
 * XMLSaver writes a {@link Bigraph} out as an XML document.
 * @author alec
 */
public class BigraphXMLSaver extends XMLSaver {
	public BigraphXMLSaver() {
		this(null);
	}
	
	public BigraphXMLSaver(ISaver parent) {
		super(parent);
		setDefaultNamespace(BIGRAPH);
	}
	
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public BigraphXMLSaver setModel(ModelObject model) {
		if (model == null || model instanceof Bigraph)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(BIGRAPH, "bigraph:bigraph"));
		processModel(getDocumentElement());
		finish();
	}
	
	@Override
	public Element processModel(Element e) throws SaveFailedException {
		Bigraph obj = getModel();
		
		appendChildIfNotNull(e,
			processOrReference(
				newElement(SIGNATURE, "signature:signature"),
				obj.getSignature(), new SignatureXMLSaver(this)));
		
		ArrayList<Element>
			edges = new ArrayList<Element>(),
			outernames = new ArrayList<Element>(),
			roots = new ArrayList<Element>(),
			innernames = new ArrayList<Element>();
		
		for (Layoutable i : obj.getChildren()) {
			Element f = null;
			if (i instanceof Edge) {
				edges.add(f = newElement(BIGRAPH, "bigraph:edge"));
			} else if (i instanceof OuterName) {
				outernames.add(f = newElement(BIGRAPH, "bigraph:outername"));
			} else if (i instanceof Root) {
				roots.add(f = processContents(
						newElement(BIGRAPH, "bigraph:root"), (Root)i));
			} else if (i instanceof InnerName) {
				innernames.add(f = processPoint(
						newElement(BIGRAPH, "bigraph:innername"), (Point)i));
			}
			applyCommonProperties(f, i);
		}
		
		appendChildren(e, edges);
		appendChildren(e, outernames);
		appendChildren(e, roots);
		appendChildren(e, innernames);
		
		return executeDecorators(obj, e);
	}
	
	private static final void
	appendChildren(Element e, List<Element> children) {
		for (Element i : children)
			if (i != null)
				e.appendChild(i);
		children.clear();
	}
	
	private Element processContents(Element e, Container c)
			throws SaveFailedException {
		ArrayList<Element>
			nodes = new ArrayList<Element>(),
			sites = new ArrayList<Element>();
		
		for (Layoutable i : c.getChildren()) {
			Element f = null;
			if (i instanceof Node) {
				nodes.add(f =
					processNode(newElement(BIGRAPH, "bigraph:node"), (Node)i));
			} else if (i instanceof Site) {
				sites.add(f = newElement(BIGRAPH, "bigraph:site"));
			}
			applyCommonProperties(f, i);
		}
		appendChildren(e, nodes);
		appendChildren(e, sites);
		return e;
	}
	
	private Element processNode(Element e, Node n) throws SaveFailedException {
		applyAttributes(e,
			"control", n.getControl().getName(),
			"name", n.getName());
		
		for (Port p : n.getPorts())
			appendChildIfNotNull(e, processPoint(
					newElement(BIGRAPH, "bigraph:port"), p));
		
		return processContents(e, n);
	}
	
	private static Element processPoint(Element e, Point p)
			throws SaveFailedException {
		Link link = p.getLink();
		if (link != null) {
			applyAttributes(e, "name", p.getName());
			applyAttributes(e, "link", link.getName());
			return e;
		} else return null;
	}
		
	private Element applyCommonProperties(Element e, Layoutable l) {
		if (e == null || l == null)
			return e;
		if (!(l instanceof Bigraph))
			applyAttributes(e, "name", l.getName());
		return executeDecorators(l, e);
	}
}
