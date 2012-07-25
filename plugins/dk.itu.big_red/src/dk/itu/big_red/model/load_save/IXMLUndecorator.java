package dk.itu.big_red.model.load_save;

import org.bigraph.model.ModelObject;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.loaders.IXMLLoader;
import org.w3c.dom.Element;

public interface IXMLUndecorator {
	void setLoader(IXMLLoader loader);
	void undecorate(ModelObject object, Element el);
	void finish(IChangeExecutor ex);
}