package org.bigraph.model.savers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bigraph.model.ModelObject;
import org.w3c.dom.Element;

public abstract class XMLSaver extends Saver {
	private List<IXMLDecorator> decorators = null;
	
	protected List<IXMLDecorator> getDecorators() {
		return (decorators != null ? decorators :
				Collections.<IXMLDecorator>emptyList());
	}
	
	public void addDecorator(IXMLDecorator d) {
		if (d == null)
			return;
		if (decorators == null)
			decorators = new ArrayList<IXMLDecorator>();
		decorators.add(d);
	}
	
	protected Element executeDecorators(ModelObject mo, Element el) {
		if (mo != null && el != null)
			for (IXMLDecorator d : getDecorators())
				d.decorate(mo, el);
		return el;
	}
}
