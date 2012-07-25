package org.bigraph.model.savers;

import org.bigraph.model.ModelObject;
import org.w3c.dom.Element;

public interface IXMLDecorator {
	void decorate(ModelObject object, Element el);
}