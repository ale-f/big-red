package dk.itu.big_red.model.load_save.savers;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;
import static
	dk.itu.big_red.model.load_save.loaders.XMLLoader.getIntAttribute;
import static
	dk.itu.big_red.model.load_save.loaders.XMLLoader.getColorAttribute;
import static
	dk.itu.big_red.model.load_save.loaders.XMLLoader.getDoubleAttribute;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.load_save.loaders.XMLLoader;
import dk.itu.big_red.model.load_save.loaders.XMLLoader.Undecorator;
import dk.itu.big_red.model.load_save.savers.XMLSaver.Decorator;

public class RedXMLDecorator implements Decorator, Undecorator {
	public static Element rectangleToElement(Element e, Rectangle r) {
		return XMLSaver.applyAttributes(e,
			"width", r.width(), "height", r.height(), "x", r.x(), "y", r.y());
	}
	
	@Override
	public void decorate(ModelObject object, Element el) {
		Document doc = el.getOwnerDocument();
		
		if (object instanceof Control) {
			Control c = (Control)object;
			Element aE = doc.createElementNS(BIG_RED, "big-red:shape");
			
			Object shape = ExtendedDataUtilities.getShape(c);
			aE.setAttributeNS(BIG_RED, "big-red:shape",
					(shape instanceof PointList ? "polygon" : "oval"));
			
			if (shape instanceof PointList) {
				PointList pl = (PointList)shape;
				for (int i = 0; i < pl.size(); i++) {
					Point p = pl.getPoint(i);
					Element pE = doc.createElementNS(BIG_RED, "big-red:point");
					pE.setAttributeNS(BIG_RED, "big-red:x", "" + p.x);
					pE.setAttributeNS(BIG_RED, "big-red:y", "" + p.y);
					aE.appendChild(pE);
				}
			}

			el.setAttributeNS(BIG_RED, "big-red:label",
					ExtendedDataUtilities.getLabel(c));
			el.appendChild(aE);
			/* continue */
		} else if (object instanceof PortSpec) {
			PortSpec p = (PortSpec)object;
			
			Element pA =
				doc.createElementNS(BIG_RED, "big-red:port-appearance");
			pA.setAttributeNS(BIG_RED, "big-red:segment", "" + ExtendedDataUtilities.getSegment(p));
			pA.setAttributeNS(BIG_RED, "big-red:distance", "" + ExtendedDataUtilities.getDistance(p));
			
			el.appendChild(pA);
			return;
		} else if (object instanceof Signature || object instanceof Bigraph ||
				object instanceof Port || object instanceof SimulationSpec ||
				object instanceof ReactionRule)
			return;
		
		Element aE = doc.createElementNS(BIG_RED, "big-red:appearance");
		
		if (object instanceof Layoutable)
			rectangleToElement(aE, ((Layoutable)object).getLayout());
		
		Colour
			fill = ExtendedDataUtilities.getFill(object),
			outline = ExtendedDataUtilities.getOutline(object);
		if (fill != null)
			aE.setAttributeNS(BIG_RED, "big-red:fillColor",
					fill.toHexString());
		if (outline != null)
			aE.setAttributeNS(BIG_RED, "big-red:outlineColor",
					outline.toHexString());
		
		String comment = ExtendedDataUtilities.getComment(object);
		if (comment.length() > 0)
			aE.setAttributeNS(BIG_RED, "big-red:comment", comment);
		
		if (aE.hasChildNodes() || aE.hasAttributes())
			el.appendChild(aE);
	}

	private Element getNamedChildElement(Element el, String ns, String ln) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node j = children.item(i);
			if (j instanceof Element && j.getNamespaceURI().equals(ns) &&
				j.getLocalName().equals(ln))
				return (Element)j;
		}
		return null;
	}
	
	@Override
	public void undecorate(ModelObject object, Element el) {
		Element eA = getNamedChildElement(el, BIG_RED, "appearance");
		if (eA != null) {
			Colour
				fill = getColorAttribute(eA, BIG_RED, "fillColor"),
				outline = getColorAttribute(eA, BIG_RED, "outlineColor");
			if (fill != null)
				ExtendedDataUtilities.setFill(object, fill);
			if (outline != null)
				ExtendedDataUtilities.setOutline(object, outline);
	
			String comment = XMLLoader.getAttributeNS(eA, BIG_RED, "comment");
			if (comment != null)
				ExtendedDataUtilities.setComment(object, comment);
		}
		
		if (object instanceof PortSpec) {
			PortSpec p = (PortSpec)object;
			Element eS = getNamedChildElement(el, BIG_RED, "port-appearance");
			if (eS != null) {
				ExtendedDataUtilities.setSegment(p,
						getIntAttribute(eS, BIG_RED, "segment"));
				ExtendedDataUtilities.setDistance(p,
						getDoubleAttribute(eS, BIG_RED, "distance"));
			}
		}
	}
}
