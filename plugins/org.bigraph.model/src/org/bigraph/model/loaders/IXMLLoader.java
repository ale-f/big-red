package org.bigraph.model.loaders;

import org.bigraph.model.ModelObject;
import org.bigraph.model.process.IParticipant;
import org.w3c.dom.Element;

public interface IXMLLoader extends IChangeLoader {
	interface Undecorator extends IParticipant {
		void undecorate(ModelObject object, Element el);
		void finish();
	}
}
