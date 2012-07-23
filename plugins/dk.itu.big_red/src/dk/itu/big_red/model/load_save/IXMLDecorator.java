package dk.itu.big_red.model.load_save;

import org.bigraph.model.ModelObject;
import org.w3c.dom.Element;

public interface IXMLDecorator {
	void decorate(ModelObject object, Element el);
}