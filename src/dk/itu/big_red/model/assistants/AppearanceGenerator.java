package dk.itu.big_red.model.assistants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.load_save.IRedNamespaceConstants;
import dk.itu.big_red.model.load_save.loaders.XMLLoader;
import dk.itu.big_red.model.load_save.savers.XMLSaver;
import dk.itu.big_red.utilities.Colour;
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
			doc.createElementNS(IRedNamespaceConstants.BIG_RED,
					"big-red:appearance");
		boolean alive = false;
		
		if (o instanceof Layoutable) {
			alive = true;
			rectangleToElement(aE, ((Layoutable)o).getLayout());
		}
		
		if (o instanceof Colourable) {
			alive = true;
			Colourable c = (Colourable)o;
			
			XMLSaver.applyAttributes(aE,
					"fillColor", new Colour(c.getFillColour()).toHexString(),
					"outlineColor", new Colour(c.getOutlineColour()).toHexString());
		}
		
		if (o instanceof ModelObject) {
			alive = true;
			String comment = ((ModelObject)o).getComment();
			if (comment != null)
				XMLSaver.applyAttributes(aE,
						"comment", comment);
		}
		
		return (alive ? aE : null);
	}
	
	public static void setAppearance(Element e, Object o, ChangeGroup cg) {
		if (!(e.getNamespaceURI().equals(IRedNamespaceConstants.BIG_RED) &&
				e.getLocalName().equals("appearance")))
			return;
		
		if (o instanceof Layoutable) {
			Layoutable l = (Layoutable)o;
			Rectangle r = elementToRectangle(e);
			cg.add(l.changeLayout(r));
		}
		
		if (o instanceof Colourable) {
			Colourable c = (Colourable)o;
			cg.add(c.changeFillColour(XMLLoader.getColorAttribute(e, IRedNamespaceConstants.BIG_RED, "fillColor")),
					c.changeOutlineColour(XMLLoader.getColorAttribute(e, IRedNamespaceConstants.BIG_RED, "outlineColor")));
		}
		
		if (o instanceof ModelObject)
			((ModelObject)o).setComment(XMLLoader.getAttributeNS(e, IRedNamespaceConstants.BIG_RED, "comment"));
	}
	
	public static Element rectangleToElement(Element e, ReadonlyRectangle r) {
		return XMLSaver.applyAttributes(e,
				"width", r.getWidth(),
				"height", r.getHeight(),
				"x", r.getX(),
				"y", r.getY());
	}
	
	public static Rectangle elementToRectangle(Element e) {
		return new Rectangle(
				XMLLoader.getIntAttribute(e, IRedNamespaceConstants.BIG_RED, "x"),
				XMLLoader.getIntAttribute(e, IRedNamespaceConstants.BIG_RED, "y"),
				XMLLoader.getIntAttribute(e, IRedNamespaceConstants.BIG_RED, "width"),
				XMLLoader.getIntAttribute(e, IRedNamespaceConstants.BIG_RED, "height"));
	}
}
