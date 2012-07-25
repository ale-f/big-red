package org.bigraph.model.loaders;

import org.bigraph.model.ModelObject;
import org.bigraph.model.changes.IChangeExecutor;
import org.w3c.dom.Element;

public interface IXMLUndecorator {
	void setLoader(IXMLLoader loader);
	void undecorate(ModelObject object, Element el);
	void finish(IChangeExecutor ex);
}