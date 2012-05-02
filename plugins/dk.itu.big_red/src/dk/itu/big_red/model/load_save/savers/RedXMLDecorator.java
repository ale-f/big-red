package dk.itu.big_red.model.load_save.savers;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;

import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.load_save.savers.XMLSaver.Decorator;

public class RedXMLDecorator implements Decorator {
	public static Element rectangleToElement(Element e, Rectangle r) {
		return XMLSaver.applyAttributes(e,
			"width", r.width(), "height", r.height(), "x", r.x(), "y", r.y());
	}
	
	@Override
	public void decorate(ModelObject object, Element el) {
		System.out.println(this + ".decorate(" + object + ", " + el + ")");
		if (object instanceof Bigraph ||
				object instanceof Port ||
				object instanceof PortSpec)
			return;
		Document doc = el.getOwnerDocument();
		
		Element aE = doc.createElementNS(BIG_RED, "big-red:appearance");
		boolean alive = false;
		
		if (object instanceof Layoutable) {
			alive = true;
			rectangleToElement(aE, ((Layoutable)object).getLayout());
		}
		
		if (object instanceof Colourable) {
			alive = true;
			Colourable c = (Colourable)object;
			
			aE.setAttributeNS(BIG_RED, "big-red:fillColor",
					c.getFillColour().toHexString());
			aE.setAttributeNS(BIG_RED, "big-red:outlineColor",
					c.getOutlineColour().toHexString());
		}
		
		String comment = ExtendedDataUtilities.getComment(object);
		if (comment != null) {
			alive = true;
			aE.setAttributeNS(BIG_RED, "big-red:comment", comment);
		}
		
		if (alive)
			el.appendChild(aE);
	}
}
