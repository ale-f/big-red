package org.bigraph.model.savers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bigraph.model.ModelObject;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
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
	
	public static final String OPTION_DEFAULT_NS = "XMLSaverDefaultNS";
	private boolean useDefaultNamespace = false;
	
	{
		addOption(OPTION_DEFAULT_NS, "Use a default namespace",
			"Don't use a namespace for the basic document elements.");
	}
	
	@Override
	protected Object getOption(String id) {
		if (OPTION_DEFAULT_NS.equals(id)) {
			return useDefaultNamespace;
		} else return super.getOption(id);
	}
	
	@Override
	protected void setOption(String id, Object value) {
		if (OPTION_DEFAULT_NS.equals(id)) {
			useDefaultNamespace = (Boolean)value;
		} else super.setOption(id, value);
	}
	
	protected boolean defNSMatch(String nsURI) {
		return useDefaultNamespace && defaultNamespace != null &&
				nsURI != null && defaultNamespace.equals(nsURI);
	}
	
	protected String unqualifyName(String name) {
		return name.substring(name.indexOf(':') + 1);
	}
	
	protected Element newElement(String nsURI, String qualifiedName) {
		if (defNSMatch(nsURI)) {
			return getDocument().createElementNS(
					nsURI, unqualifyName(qualifiedName));
		} else return getDocument().createElementNS(nsURI, qualifiedName);
	}
	
	protected XMLSaver finish() throws SaveFailedException {
		try {
			Source source = new DOMSource(getDocument());
			Result result = new StreamResult(getOutputStream());
			
			Transformer t = getSharedTransformerFactory().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			t.transform(source, result);
			getOutputStream().close();
			
			return this;
		} catch (Exception e) {
			throw new SaveFailedException(e);
		}
	}
	
	private String defaultNamespace = null;
	
	protected void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}
	
	protected String getDefaultNamespace() {
		return defaultNamespace;
	}
	
	protected static TransformerFactory getSharedTransformerFactory() {
		return tf;
	}
	
	protected static DOMImplementation getSharedDOMImplementation() {
		return impl;
	}
	
	private Document doc = null;
	
	public Document getDocument() {
		return doc;
	}
	
	public Element getDocumentElement() {
		if (getDocument() != null) {
			return getDocument().getDocumentElement();
		} else return null;
	}
	
	public XMLSaver setDocument(Document doc) {
		this.doc = doc;
		return this;
	}
	
	@Override
	public boolean canExport() {
		return (super.canExport() && doc != null);
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
