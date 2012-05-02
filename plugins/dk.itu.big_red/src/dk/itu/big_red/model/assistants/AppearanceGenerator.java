package dk.itu.big_red.model.assistants;

import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Element;

import dk.itu.big_red.model.load_save.IRedNamespaceConstants;
import dk.itu.big_red.model.load_save.loaders.XMLLoader;

public final class AppearanceGenerator {
	private AppearanceGenerator() {}
	
	public static Rectangle elementToRectangle(Element e) {
		return new Rectangle(
				XMLLoader.getIntAttribute(e, IRedNamespaceConstants.BIG_RED, "x"),
				XMLLoader.getIntAttribute(e, IRedNamespaceConstants.BIG_RED, "y"),
				XMLLoader.getIntAttribute(e, IRedNamespaceConstants.BIG_RED, "width"),
				XMLLoader.getIntAttribute(e, IRedNamespaceConstants.BIG_RED, "height"));
	}
}
