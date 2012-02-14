package dk.itu.big_red.model.assistants;

import org.w3c.dom.Element;

import dk.itu.big_red.model.load_save.IRedNamespaceConstants;
import dk.itu.big_red.model.load_save.loaders.XMLLoader;
import dk.itu.big_red.model.load_save.savers.XMLSaver;
import dk.itu.big_red.utilities.geometry.ReadonlyRectangle;
import dk.itu.big_red.utilities.geometry.Rectangle;

public final class AppearanceGenerator {
	private AppearanceGenerator() {}
	
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
