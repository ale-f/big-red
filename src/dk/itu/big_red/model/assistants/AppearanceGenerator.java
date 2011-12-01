package dk.itu.big_red.model.assistants;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.import_export.XMLNS;
import dk.itu.big_red.util.Colour;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.geometry.ReadonlyRectangle;
import dk.itu.big_red.util.geometry.Rectangle;

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
		
		if (o instanceof Layoutable) {
			alive = true;
			ReadonlyRectangle r = ((Layoutable)o).getLayout();
			
			DOM.applyAttributes(aE,
					"width", r.getWidth(),
					"height", r.getHeight(),
					"x", r.getX(),
					"y", r.getY());
		}
		
		if (o instanceof Colourable) {
			alive = true;
			Colourable c = (Colourable)o;
			
			DOM.applyAttributes(aE,
					"fillColor", new Colour(c.getFillColour()).toHexString(),
					"outlineColor", new Colour(c.getOutlineColour()).toHexString());
		}
		
		if (o instanceof ModelObject) {
			alive = true;
			String comment = ((ModelObject)o).getComment();
			if (comment != null)
				DOM.applyAttributes(aE,
						"comment", comment);
		}
		
		return (alive ? aE : null);
	}
	
	public static void setAppearance(Element e, Object o, ChangeGroup cg) {
		if (!DOM.nameEqualsNS(e, XMLNS.BIG_RED, "appearance"))
			return;
		
		if (o instanceof Layoutable) {
			Layoutable l = (Layoutable)o;
			Rectangle r = new Rectangle(
					DOM.getIntAttribute(e, XMLNS.BIG_RED, "x"),
					DOM.getIntAttribute(e, XMLNS.BIG_RED, "y"),
					DOM.getIntAttribute(e, XMLNS.BIG_RED, "width"),
					DOM.getIntAttribute(e, XMLNS.BIG_RED, "height"));
			
			cg.add(l.changeLayout(r));
		}
		
		if (o instanceof Colourable) {
			Colourable c = (Colourable)o;
			cg.add(c.changeFillColour(DOM.getColorAttribute(e, XMLNS.BIG_RED, "fillColor")),
					c.changeOutlineColour(DOM.getColorAttribute(e, XMLNS.BIG_RED, "outlineColor")));
		}
		
		if (o instanceof ModelObject)
			((ModelObject)o).setComment(DOM.getAttributeNS(e, XMLNS.BIG_RED, "comment"));
	}
	
	public static Element getShape(Document doc, Control c) {
		Element aE =
			doc.createElementNS(XMLNS.BIG_RED,
					"big-red:shape");

		DOM.applyAttributes(aE,
				"shape", (c.getShape() == Shape.POLYGON ? "polygon" : "oval"));
		
		PointList pl = c.getPoints();
		if (pl != null) {
			for (int i = 0; i < pl.size(); i++) {
				Point p = pl.getPoint(i);
				Element pE = doc.createElement("big-red:point");
				DOM.applyAttributes(pE,
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

		Control.Shape shape = Shape.OVAL;
		PointList pl = null;
		
		String s = DOM.getAttributeNS(e, XMLNS.BIG_RED, "shape");
		if (s != null) {
			if (s.equals("polygon"))
				shape = Shape.POLYGON;
		}
		
		if (shape == Shape.POLYGON) {
			pl = new PointList();
			for (Element pE : DOM.getChildElements(e))
				pl.addPoint(DOM.getIntAttribute(pE, XMLNS.BIG_RED, "x"),
						DOM.getIntAttribute(pE, XMLNS.BIG_RED, "y"));
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
