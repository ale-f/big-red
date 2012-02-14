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
import dk.itu.big_red.model.load_save.XMLLoader;
import dk.itu.big_red.model.load_save.XMLNS;
import dk.itu.big_red.utilities.Colour;
import dk.itu.big_red.utilities.DOM;
import dk.itu.big_red.utilities.geometry.ReadonlyRectangle;
import dk.itu.big_red.utilities.geometry.Rectangle;

public final class AppearanceGenerator {
	private AppearanceGenerator() {}
	
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
			rectangleToElement(aE, ((Layoutable)o).getLayout());
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
		if (!nameEqualsNS(e, XMLNS.BIG_RED, "appearance"))
			return;
		
		if (o instanceof Layoutable) {
			Layoutable l = (Layoutable)o;
			Rectangle r = elementToRectangle(e);
			cg.add(l.changeLayout(r));
		}
		
		if (o instanceof Colourable) {
			Colourable c = (Colourable)o;
			cg.add(c.changeFillColour(XMLLoader.getColorAttribute(e, XMLNS.BIG_RED, "fillColor")),
					c.changeOutlineColour(XMLLoader.getColorAttribute(e, XMLNS.BIG_RED, "outlineColor")));
		}
		
		if (o instanceof ModelObject)
			((ModelObject)o).setComment(XMLLoader.getAttributeNS(e, XMLNS.BIG_RED, "comment"));
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
		if (!nameEqualsNS(e, XMLNS.BIG_RED, "shape"))
			return;

		Control.Shape shape = Shape.OVAL;
		PointList pl = null;
		
		String s = XMLLoader.getAttributeNS(e, XMLNS.BIG_RED, "shape");
		if (s != null) {
			if (s.equals("polygon"))
				shape = Shape.POLYGON;
		}
		
		if (shape == Shape.POLYGON) {
			pl = new PointList();
			for (Element pE : XMLLoader.getChildElements(e))
				pl.addPoint(XMLLoader.getIntAttribute(pE, XMLNS.BIG_RED, "x"),
						XMLLoader.getIntAttribute(pE, XMLNS.BIG_RED, "y"));
		}
		
		c.setShape(shape);
		c.setPoints(pl);
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
	
	public static Element rectangleToElement(Element e, ReadonlyRectangle r) {
		return DOM.applyAttributes(e,
				"width", r.getWidth(),
				"height", r.getHeight(),
				"x", r.getX(),
				"y", r.getY());
	}
	
	public static Rectangle elementToRectangle(Element e) {
		return new Rectangle(
				XMLLoader.getIntAttribute(e, XMLNS.BIG_RED, "x"),
				XMLLoader.getIntAttribute(e, XMLNS.BIG_RED, "y"),
				XMLLoader.getIntAttribute(e, XMLNS.BIG_RED, "width"),
				XMLLoader.getIntAttribute(e, XMLNS.BIG_RED, "height"));
	}

	private static boolean nameEqualsNS(Element e, String nsURI, String nodeName) {
		return (e.getNamespaceURI().equals(nsURI) && e.getLocalName().equals(nodeName));
	}
}
