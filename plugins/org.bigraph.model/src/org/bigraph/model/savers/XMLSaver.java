package org.bigraph.model.savers;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

public abstract class XMLSaver extends Saver implements IXMLSaver {
	public XMLSaver() {
	}
	
	public XMLSaver(ISaver parent) {
		super(parent);
		if (parent instanceof XMLSaver)
			setDocument(((XMLSaver)parent).getDocument());
	}
	
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
		addOption(new SaverOption("Use a default namespace",
				"Don't use a namespace for the basic document elements.") {
			@Override
			public Object get() {
				return useDefaultNamespace;
			}
			
			@Override
			public void set(Object value) {
				useDefaultNamespace = (Boolean)value;
			}
		});
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
	
	@Override
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
	
	public abstract Element processModel(Element e) throws SaveFailedException;
	
	protected Element processOrReference(
			Element e, ModelObject object, XMLSaver ex)
			throws SaveFailedException {
		IFileWrapper f;
		if (e == null || object == null) {
			return null;
		} else if (getFile() != null &&
				(f = FileData.getFile(object)) != null) {
			e.setAttributeNS(null,
				"src", f.getRelativePath(getFile().getParent().getPath()));
			/* No decoration takes place! */
		} else {
			ex.setModel(object).setFile(getFile());
			ex.processModel(e);
		}
		return e;
	}

	/**
	 * Applies the specified name-value pairs to the specified element as
	 * attributes. (This uses {@link Element#setAttribute}, but is slightly
	 * less irritating, as it automatically converts names and values to
	 * strings.)
	 * @param d an Element
	 * @param attrs a vararg list of name-value pairs of any type
	 * @return d, for convenience
	 * @see #applyAttributesNS(Element, Object...)
	 */
	protected static Element applyAttributes(Element d, Object... attrs) {
		for (int i = 0; i < attrs.length; i += 2) {
			Object
				attrName = attrs[i],
				attrValue = attrs[i + 1];
			if (attrName != null && attrValue != null)
				d.setAttributeNS(null,
						attrName.toString(), attrValue.toString());
		}
		return d;
	}

	/**
	 * Appends <code>newChild</code> to <code>e</code>, if neither of them are
	 * <code>null</code>.
	 * @param e the would-be parent of the new node
	 * @param newChild the node to add
	 */
	protected static void appendChildIfNotNull(Element e, Node newChild) {
		if (e != null && newChild != null)
			e.appendChild(newChild);
	}

	/**
	 * Creates a {@link Document} (with no {@link DocumentType}) using the
	 * shared DOM implementation.
	 * @param ns the namespace URI of the document to create
	 * @param qName the qualified name of the root element
	 * @return a new {@link Document}
	 */
	protected Document createDocument(String ns, String qName) {
		DOMImplementation impl = getSharedDOMImplementation();
		if (defNSMatch(ns)) {
			return impl.createDocument(ns, unqualifyName(qName), null);
		} else return impl.createDocument(ns, qName, null);
	}
	
	@Override
	public boolean canExport() {
		return (super.canExport() && doc != null);
	}
	
	protected Element executeDecorators(ModelObject mo, Element el) {
		if (mo != null && el != null)
			for (Decorator d : getParticipants(Decorator.class))
				d.decorate(mo, el);
		return el;
	}
}
