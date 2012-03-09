package dk.itu.big_red.model.load_save.savers;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.IRedNamespaceConstants;
import dk.itu.big_red.model.namespaces.INamePolicy;
import dk.itu.big_red.model.namespaces.LongNamePolicy;

public class SignatureXMLSaver extends XMLSaver {
	@Override
	public Signature getModel() {
		return (Signature)super.getModel();
	}
	
	@Override
	public SignatureXMLSaver setModel(ModelObject model) {
		if (model instanceof Signature)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(IRedNamespaceConstants.SIGNATURE, "signature:signature"));
		processObject(getDocumentElement(), getModel());
		finish();
	}

	@Override
	public Element processObject(Element e, Object s_) throws SaveFailedException {
		if (!(s_ instanceof Signature))
			throw new SaveFailedException(s_ + " isn't a Signature");
		Signature s = (Signature)s_;
		
		applyAttributes(e,
			"xmlns:big-red", IRedNamespaceConstants.BIG_RED,
			"xmlns:signature", IRedNamespaceConstants.SIGNATURE);

		for (Control c : s.getControls())
			appendChildIfNotNull(e,
				processControl(newElement(IRedNamespaceConstants.SIGNATURE, "signature:control"), c));
		return e;
	}
	
	private Element processControl(Element e, Control c) {
		applyAttributes(e,
				"name", c.getName(),
				"kind", c.getKind().toString());
		
		INamePolicy parameterPolicy = c.getParameterPolicy();
		if (parameterPolicy instanceof LongNamePolicy)
			applyAttributes(e, "parameter", "LONG");
		
		for (Port p : c.createPorts())
			e.appendChild(processPort(
				newElement(IRedNamespaceConstants.SIGNATURE, "signature:port"), p));
		
		appendChildIfNotNull(e, shapeToElement(getDocument(), c));
		appendChildIfNotNull(e,
				BigraphXMLSaver.appearanceToElement(getDocument(), c));
		e.setAttributeNS(IRedNamespaceConstants.BIG_RED,
				"big-red:label", c.getLabel());
		
		return e;
	}
	
	private Element processPort(Element e, Port p) {
		applyAttributes(e,
				"name", p.getName());
		
		e.appendChild(
			applyAttributes(
				newElement(IRedNamespaceConstants.BIG_RED, "big-red:port-appearance"),
				"segment", p.getSegment(),
				"distance", p.getDistance()));
		
		return e;
	}

	private static Element shapeToElement(Document doc, Control c) {
		Element aE =
			doc.createElementNS(IRedNamespaceConstants.BIG_RED,
					"big-red:shape");
	
		XMLSaver.applyAttributes(aE,
				"shape", (c.getShape() == Shape.POLYGON ? "polygon" : "oval"));
		
		PointList pl = c.getPoints();
		if (pl != null) {
			for (int i = 0; i < pl.size(); i++) {
				Point p = pl.getPoint(i);
				Element pE = doc.createElement("big-red:point");
				XMLSaver.applyAttributes(pE,
						"x", p.x,
						"y", p.y);
				aE.appendChild(pE);
			}
		}
		
		return aE;
	}
}
