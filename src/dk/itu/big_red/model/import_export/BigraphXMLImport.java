package dk.itu.big_red.model.import_export;

import org.eclipse.draw2d.geometry.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Signature;
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
public class BigraphXMLImport extends ModelImport<Bigraph> {
	@Override
	public Bigraph importObject() throws ImportFailedException {
		try {
			Document d = DOM.parse(source);
			return (Bigraph)process(d.getDocumentElement());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}
	
	private Bigraph bigraph = null;
	
	private void processBigraph(Element e, Bigraph model) throws ImportFailedException {
		String signaturePath = e.getAttribute("signature");
		System.out.println(signaturePath);
		
		Signature signature = model.getSignature();
		
    	dk.itu.big_red.model.Control b =
    		signature.addControl(new Control("Building", "B", Shape.SHAPE_OVAL, null,
    				new Point(250, 250), true)),
    	r = signature.addControl(new Control("Room", "R", Shape.SHAPE_OVAL, null,
    			new Point(125, 200), true)),
    	a = signature.addControl(new Control("Agent", "A", Shape.SHAPE_POLYGON,
    			dk.itu.big_red.model.Control.POINTS_TRIANGLE,
    			new Point(25, 50), false)),
    	c = signature.addControl(new Control("Computer", "C", Shape.SHAPE_POLYGON,
    			dk.itu.big_red.model.Control.POINTS_QUAD, new Point(25, 13), false));
		
    	b.addPort("a", 0, 0.33);
    	
    	c.addPort("b", 0, 0.45);
    	
    	a.addPort("c", 0, 0.66);
    	
    	r.addPort("d", 0, 0.78);

    	bigraph = model;
		processThing(e, model);
	}
	
	private void processThing(Element e, Thing model) throws ImportFailedException {
		Element el = (Element)DOM.getNamedChildElement(e, "big-red:appearance");
		if (el != null) {
			AppearanceGenerator.setAppearance(el, model);
			el.getParentNode().removeChild(el);
		}
		
		if (model instanceof Node) {
			Node node = (Node)model;
			node.setControl(bigraph.getSignature().getControl(DOM.getAttribute(e, "control")));
		}
		
		for (int j = 0; j < e.getChildNodes().getLength(); j++) {
			if (!(e.getChildNodes().item(j) instanceof Element))
				continue;
			Object i = process((Element)e.getChildNodes().item(j));
			if (i instanceof ILayoutable)
				model.addChild((ILayoutable)i);
		}
	}
	
	private void processPort(Element e, Port model) throws ImportFailedException {
		String name = DOM.getAttribute(e, "name"),
	           link = DOM.getAttribute(e, "link");
		model.setName(name);
		
		Edge edge = (Edge)bigraph.getNamespaceManager().getObject(Edge.class, link);
		if (edge != null) {
			edge.addPoint(model);
		} else {
			Edge ed = new Edge();
			ed.setParent(bigraph);
			ed.setName(link);
			ed.addPoint(model);
		}
	}
	
	private void processEdge(Element e, Edge model) throws ImportFailedException {
		String name = DOM.getAttribute(e, "name");
		Edge edge = (Edge)bigraph.getNamespaceManager().getObject(Edge.class, name);
		if (edge == null) {
			model.setParent(bigraph);
			model.setName(name);
			edge = model;
		}
		
		Element el = (Element)DOM.getNamedChildElement(e, "big-red:appearance");
		if (el != null) {
			AppearanceGenerator.setAppearance(el, edge);
			el.getParentNode().removeChild(el);
		}
	}
	
	private void processInnerName(Element e, InnerName model) throws ImportFailedException {
		System.out.println("INNER NAME PROCESSING BEGINS");
		Element el = (Element)DOM.getNamedChildElement(e, "big-red:appearance");
		if (el != null) {
			AppearanceGenerator.setAppearance(el, model);
			el.getParentNode().removeChild(el);
		}

		String name = DOM.getAttribute(e, "name"),
        link = DOM.getAttribute(e, "link");
		model.setParent(bigraph);
		model.setName(name);
		
		Edge edge = (Edge)bigraph.getNamespaceManager().getObject(Edge.class, link);
		if (edge != null) {
			edge.addPoint(model);
		} else {
			Edge ed = new Edge();
			ed.setParent(bigraph);
			ed.setName(link);
			ed.addPoint(model);
		}
	}
	
	private Object process(Element e) throws ImportFailedException {
		Object model = ModelFactory.getNewObject(e.getNodeName());
		if (model instanceof Bigraph) {
			processBigraph(e, (Bigraph)model);
		} else if (model instanceof Thing) {
			processThing(e, (Thing)model);
		} else if (model instanceof Port) {
			processPort(e, (Port)model);
		} else if (model instanceof Edge) {
			processEdge(e, (Edge)model);
		} else if (model instanceof InnerName) {
			processInnerName(e, (InnerName)model);
		}
		
		return model;
	}
}
