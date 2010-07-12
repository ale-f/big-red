package dk.itu.big_red.model.import_export;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.util.DOM;

public class SignatureXMLExport extends Export {
	private Signature model = null;
	
	@Override
	public void setModel(Object model) {
		if (model instanceof Signature)
			this.model = (Signature)model;
	}

	@Override
	public boolean canExport() {
		return (target != null && model != null);
	}

	private Document doc = null;
	
	@Override
	public void exportModel() throws ExportFailedException {
		process(model);
		
		try {
			DOM.write(target, doc);
		} catch (Exception e) {
			throw new ExportFailedException(e);
		}
	}

	private void process(Signature s) {
		DOMImplementation impl = DOM.getImplementation();
		doc = impl.createDocument(
				"http://pls.itu.dk/bigraphs/2010/signature", "signature", null);
		Element e = doc.getDocumentElement();
		DOM.applyAttributesToElement(e,
			"xmlns:big-red", "http://pls.itu.dk/bigraphs/2010/big-red");

		for (Control c : s.getControls())
			DOM.appendChildIfNotNull(e, process(c));
	}
	
	private Element process(Control c) {
		if (c.getLongName().equals("Unknown"))
			return null;
		
		Element e = doc.createElement("control");
		DOM.applyAttributesToElement(e,
				"name", c.getLongName());
		
		for (Port p : c.getPortsArray())
			e.appendChild(process(p));
		
		Element cs = doc.createElement("big-red:control-shape");
		DOM.applyAttributesToElement(cs,
				"shape", (c.getShape() == Shape.SHAPE_POLYGON ? "polygon" : "oval"));
		if (c.getShape() == Shape.SHAPE_POLYGON) {
			PointList points = c.getPoints();
			for (int i = 0; i < points.size(); i++) {
				Point p = points.getPoint(i);
				Element pt = doc.createElement("big-red:point");
				DOM.applyAttributesToElement(pt,
						"x", p.x,
						"y", p.y);
				cs.appendChild(pt);
			}
		}
		e.appendChild(cs);
		
		DOM.appendChildIfNotNull(e, AppearanceGenerator.getAppearance(doc, c));
		
		return e;
	}
	
	private Element process(Port p) {
		Element e = doc.createElement("port");
		DOM.applyAttributesToElement(e,
				"name", p.getName());
		
		Element pa = doc.createElement("big-red:port-appearance");
		DOM.applyAttributesToElement(pa,
				"segment", p.getSegment(),
				"distance", p.getDistance());
		e.appendChild(pa);
		
		return e;
	}
}
