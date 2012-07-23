package dk.itu.big_red.model;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Control;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Port;
import org.bigraph.model.PortSpec;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.editors.assistants.Colour;
import dk.itu.big_red.editors.assistants.ColourUtilities;
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.assistants.LayoutUtilities;
import dk.itu.big_red.model.load_save.IXMLDecorator;
import dk.itu.big_red.model.load_save.savers.XMLSaver;

public class RedXMLDecorator implements IXMLDecorator {
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
		
		if (object instanceof Layoutable) {
			Rectangle layout =
					LayoutUtilities.getLayoutRaw((Layoutable)object);
			if (layout != null)
				rectangleToElement(aE, layout);
		}
		
		Colour
			fill = ColourUtilities.getFillRaw(object),
			outline = ColourUtilities.getOutlineRaw(object);
		if (fill != null)
			aE.setAttributeNS(BIG_RED, "big-red:fillColor",
					fill.toHexString());
		if (outline != null)
			aE.setAttributeNS(BIG_RED, "big-red:outlineColor",
					outline.toHexString());
		
		String comment = ExtendedDataUtilities.getComment(object);
		if (comment != null)
			aE.setAttributeNS(BIG_RED, "big-red:comment", comment);
		
		if (aE.hasChildNodes() || aE.hasAttributes())
			el.appendChild(aE);
	}
}