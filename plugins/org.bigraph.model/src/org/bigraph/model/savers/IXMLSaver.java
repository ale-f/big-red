package org.bigraph.model.savers;

import org.bigraph.model.ModelObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface IXMLSaver extends ISaver {
	interface Decorator {
		Decorator newInstance();
		
		void setSaver(IXMLSaver saver);
		void decorate(ModelObject object, Element el);
	}
	
	Document getDocument();
}
