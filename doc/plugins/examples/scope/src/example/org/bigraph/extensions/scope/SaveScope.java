package example.org.bigraph.extensions.scope;

import org.bigraph.model.ModelObject;
import org.bigraph.model.PortSpec;
import org.bigraph.model.savers.ISaver;
import org.bigraph.model.savers.IXMLSaver.Decorator;
import org.w3c.dom.Element;

public class SaveScope implements Decorator {
	@Override
	public void setSaver(ISaver saver) {
		/* do nothing */
	}

	static final String XMLNS =
			"http://bigraph.org.example/xmlns/2012/scope";
	
	@Override
	public void decorate(ModelObject object, Element el) {
		if (object instanceof PortSpec) {
			if (Scope.isScoped((PortSpec)object))
				el.setAttributeNS(XMLNS, "scope:scope", "true");
		}
	}
}
