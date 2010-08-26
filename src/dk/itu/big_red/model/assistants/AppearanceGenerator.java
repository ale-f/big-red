package dk.itu.big_red.model.assistants;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.import_export.XMLNS;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.IFillColourable;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.model.interfaces.internal.IOutlineColourable;
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
			doc.createElementNS(XMLNS.BIG_RED,
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
		
		if (o instanceof IFillColourable) {
			alive = true;
			DOM.applyAttributesToElement(aE,
					"fillColor", Utility.colourToString(((IFillColourable)o).getFillColour()));
		}
		
		if (o instanceof IOutlineColourable) {
			alive = true;
			DOM.applyAttributesToElement(aE,
					"outlineColor", Utility.colourToString(((IOutlineColourable)o).getOutlineColour()));
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
		if (!DOM.nameEqualsNS(e, XMLNS.BIG_RED, "appearance"))
			return;
		
		if (o instanceof ILayoutable) {
			Rectangle r = new Rectangle(
					DOM.getIntAttribute(e, XMLNS.BIG_RED, "x"),
					DOM.getIntAttribute(e, XMLNS.BIG_RED, "y"),
					DOM.getIntAttribute(e, XMLNS.BIG_RED, "width"),
					DOM.getIntAttribute(e, XMLNS.BIG_RED, "height"));
			
			((ILayoutable)o).setLayout(r);
		}
		
		if (o instanceof IFillColourable)
			((IFillColourable)o).setFillColour(DOM.getColorAttribute(e, XMLNS.BIG_RED, "fillColor"));
		
		if (o instanceof IOutlineColourable) {
			((IOutlineColourable)o).setOutlineColour(DOM.getColorAttribute(e, XMLNS.BIG_RED, "outlineColor"));
		}
		
		if (o instanceof ICommentable) {
			((ICommentable)o).setComment(DOM.getAttributeNS(e, XMLNS.BIG_RED, "comment"));
		}
	}
	
	public static Element getShape(Document doc, Control c) {
		Element aE =
			doc.createElementNS(XMLNS.BIG_RED,
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
		if (!DOM.nameEqualsNS(e, XMLNS.BIG_RED, "shape"))
			return;

		Control.Shape shape = Shape.SHAPE_OVAL;
		PointList pl = null;
		
		String s = DOM.getAttributeNS(e, XMLNS.BIG_RED, "shape");
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
				pl.addPoint(DOM.getIntAttribute(pE, XMLNS.BIG_RED, "x"),
						DOM.getIntAttribute(pE, XMLNS.BIG_RED, "y"));
			}
		}
		
		c.setShape(shape, pl);
	}
	
	public static void modelToAttributes(Element e, Object o) {
		if (o instanceof Control)
			e.setAttributeNS(XMLNS.BIG_RED, "big-red:label",
					((Control)o).getLabel());
	}
	
	public static void attributesToModel(Element e, Object o) {
		if (o instanceof Control && e.hasAttributeNS(XMLNS.BIG_RED, "label"))
			((Control)o).setLabel(e.getAttributeNS(XMLNS.BIG_RED, "label"));
	}
}
