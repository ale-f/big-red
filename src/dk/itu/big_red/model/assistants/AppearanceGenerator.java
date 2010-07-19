package dk.itu.big_red.model.assistants;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.interfaces.IColourable;
import dk.itu.big_red.model.interfaces.ICommentable;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.Utility;

public class AppearanceGenerator {
	/**
	 * Builds a <code>&lt;big-red:appearance&gt;</code> tag containing all of
	 * the Big Red-specific metadata appropriate for the given object.
	 * @param doc the {@link Document} that will contain the tag 
	 * @param o a model object
	 */
	public static Element getAppearance(Document doc, Object o) {
		if (o instanceof Bigraph)
			return null;
		
		Element aE =
			doc.createElementNS("http://pls.itu.dk/bigraphs/2010/big-red",
					"big-red:appearance");
		boolean alive = false;
		
		if (o instanceof ILayoutable) {
			alive = true;
			Rectangle r = ((ILayoutable)o).getLayout();
			
			DOM.applyAttributesToElement(aE,
					"width", r.width,
					"height", r.height,
					"x", r.x,
					"y", r.y);
		}
		
		if (o instanceof IColourable) {
			alive = true;
			RGB fillColour = ((IColourable)o).getFillColour(),
			    outlineColour = ((IColourable)o).getOutlineColour();
			
			DOM.applyAttributesToElement(aE,
					"fillColor", Utility.colourToString(fillColour),
					"outlineColor", Utility.colourToString(outlineColour));
		}
		
		if (o instanceof ICommentable) {
			alive = true;
			String comment = ((ICommentable)o).getComment();
			if (comment != null)
				DOM.applyAttributesToElement(aE,
						"comment", comment);
		}
		
		return (alive ? aE : null);
	}
	
	public static void setAppearance(Element e, Object o) {
		if (!e.getNodeName().equals("big-red:appearance"))
			return;
		
		if (o instanceof ILayoutable) {
			Rectangle r = new Rectangle(
					DOM.getIntAttribute(e, "x"),
					DOM.getIntAttribute(e, "y"),
					DOM.getIntAttribute(e, "width"),
					DOM.getIntAttribute(e, "height"));
			
			((ILayoutable)o).setLayout(r);
		}
		
		if (o instanceof IColourable) {
			RGB fillColour = DOM.getColorAttribute(e, "fillColor"),
			    outlineColour = DOM.getColorAttribute(e, "outlineColor");
			
			((IColourable)o).setFillColour(fillColour);
			((IColourable)o).setOutlineColour(outlineColour);
		}
		
		if (o instanceof ICommentable) {
			((ICommentable)o).setComment(DOM.getAttribute(e, "comment"));
		}
	}
	
	public static Element getShape(Document doc, Control c) {
		Element aE =
			doc.createElementNS("http://pls.itu.dk/bigraphs/2010/big-red",
					"big-red:shape");

		DOM.applyAttributesToElement(aE,
				"shape", (c.getShape() == Shape.SHAPE_POLYGON ? "polygon" : "oval"));
		
		PointList pl = c.getPoints();
		if (pl != null) {
			for (int i = 0; i < pl.size(); i++) {
				Point p = pl.getPoint(i);
				Element pE = doc.createElement("big-red:point");
				DOM.applyAttributesToElement(pE,
						"x", p.x,
						"y", p.y);
				aE.appendChild(pE);
			}
		}
		
		return aE;
	}
	
	public static void setShape(Element e, Control c) {
		if (!e.getTagName().equals("big-red:shape"))
			return;

		Control.Shape shape = Shape.SHAPE_OVAL;
		PointList pl = null;
		
		String s = DOM.getAttribute(e, "shape");
		if (s != null) {
			if (s.equals("polygon"))
				shape = Shape.SHAPE_POLYGON;
		}
		
		if (shape == Shape.SHAPE_POLYGON) {
			pl = new PointList();
			for (int j = 0; j < e.getChildNodes().getLength(); j++) {
				if (!(e.getChildNodes().item(j) instanceof Element))
					continue;
				Element pE = (Element)e.getChildNodes().item(j);
				pl.addPoint(DOM.getIntAttribute(pE, "x"),
						DOM.getIntAttribute(pE, "y"));
			}
		}
		
		c.setShape(shape, pl);
	}
}
