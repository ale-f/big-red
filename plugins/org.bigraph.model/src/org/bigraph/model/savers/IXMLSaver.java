package org.bigraph.model.savers;

import org.bigraph.model.ModelObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface IXMLSaver extends ISaver {
	interface Decorator extends Participant {
		void decorate(ModelObject object, Element el);
	}
	
	Document getDocument();
}
