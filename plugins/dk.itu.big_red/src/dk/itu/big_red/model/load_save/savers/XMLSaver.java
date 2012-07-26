package dk.itu.big_red.model.load_save.savers;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.resources.IFileWrapper;
import org.bigraph.model.savers.SaveFailedException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import dk.itu.big_red.model.load_save.SaverUtilities;

public abstract class XMLSaver extends org.bigraph.model.savers.XMLSaver {
	public XMLSaver() {
		SaverUtilities.installDecorators(this);
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
	
	private String defaultNamespace = null;
	
	protected void setDefaultNamespace(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}
	
	protected String getDefaultNamespace() {
		return defaultNamespace;
	}
	
	private boolean defNSMatch(String nsURI) {
		return useDefaultNamespace && defaultNamespace != null &&
				nsURI != null && defaultNamespace.equals(nsURI);
	}
	
	private String unqualifyName(String name) {
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
	
	public abstract Element processObject(Element e, Object object)
		throws SaveFailedException;
	
	protected Element processOrReference(
		Element e, ModelObject object, Class<? extends XMLSaver> klass) {
		IFileWrapper f;
		if (e == null || object == null) {
			return null;
		} else if (getFile() != null &&
				(f = FileData.getFile(object)) != null) {
			e.setAttributeNS(null,
				"src", f.getRelativePath(getFile().getParent().getPath()));
			/* No decoration takes place! */
		} else {
			XMLSaver ex;
			try {
				ex = klass.newInstance();
				ex.setDocument(getDocument()).setModel(object);
				ex.processObject(e, object);
			} catch (Exception exc) {
				return e;
			}
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
	public static Element applyAttributes(Element d, Object... attrs) {
		for (int i = 0; i < attrs.length; i += 2)
			d.setAttribute(attrs[i].toString(), attrs[i + 1].toString());
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
}