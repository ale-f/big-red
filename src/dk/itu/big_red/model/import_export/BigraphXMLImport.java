package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.Project;

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
		
		IFile sigFile =
			Project.findFileByPath(null, new Path(signaturePath));
		SignatureXMLImport si = new SignatureXMLImport();
		try {
			si.setInputStream(sigFile.getContents());
			model.setSignature(sigFile, si.importObject());
		} catch (Exception ex) {
			throw new ImportFailedException(ex);
		}
		
    	bigraph = model;
		processThing(e, model);
	}
	
	private void processThing(Element e, Thing model) throws ImportFailedException {
		Element el = DOM.removeNamedChildElement(e, "big-red:appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model);
		
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
		
		Element el = DOM.removeNamedChildElement(e, "big-red:appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, edge);
	}
	
	private void processInnerName(Element e, InnerName model) throws ImportFailedException {
		Element el = DOM.removeNamedChildElement(e, "big-red:appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model);

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
