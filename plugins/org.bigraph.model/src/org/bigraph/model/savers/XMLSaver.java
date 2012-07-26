package org.bigraph.model.savers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.bigraph.model.ModelObject;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

public abstract class XMLSaver extends Saver {
	private static final TransformerFactory tf;
	private static final DOMImplementation impl;
	static {
		DOMImplementation impl_;
		try {
			impl_ = DOMImplementationRegistry.newInstance().
					getDOMImplementation("XML 3.0");
		} catch (Exception e) {
			e.printStackTrace();
			impl_ = null;
		}
		impl = impl_;
		
		TransformerFactory tf_;
		try {
			tf_ = TransformerFactory.newInstance();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
			tf_ = null;
		}
		tf = tf_;
	}
	
	protected static TransformerFactory getSharedTransformerFactory() {
		return tf;
	}
	
	protected static DOMImplementation getSharedDOMImplementation() {
		return impl;
	}
	
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
