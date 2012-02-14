package dk.itu.big_red.model.load_save;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IResource;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.utilities.resources.IFileBackable;
import dk.itu.big_red.utilities.resources.Project;

public abstract class XMLSaver extends Saver {
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
	
	protected Element newElement(String nsURI, String qualifiedName) {
		return doc.createElementNS(nsURI, qualifiedName);
	}
	
	private static TransformerFactory tf;
	private static DOMImplementation impl = null;
	
	protected XMLSaver finish() throws SaveFailedException {
		try {
			if (tf == null)
				tf = TransformerFactory.newInstance();
			
			Source source = new DOMSource(getDocument());
			Result result = new StreamResult(getOutputStream());
			
			Transformer t = tf.newTransformer();
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
		Element e, IResource relativeTo, IFileBackable object,
		Class<? extends XMLSaver> klass) {
		if (e == null || object == null) {
			return null;
		} else if (object.getFile() != null) {
			e.setAttributeNS(null, "src",
					Project.getRelativePath(
						relativeTo, object.getFile()).toString());	
		} else {
			XMLSaver ex;
			try {
				ex = klass.newInstance();
				ex.setDocument(getDocument()).setModel((ModelObject)object);
				ex.processObject(e, object);
			} catch (Exception exc) {
				return e;
			}
		}
		return e;
	}

	/**
	 * Creates a {@link Document} (with no {@link DocumentType}) using the
	 * shared DOM implementation.
	 * @param ns the namespace URI of the document to create
	 * @param qualifiedName the qualified name of the root element
	 * @return a new {@link Document}
	 */
	protected static Document createDocument(String ns, String qualifiedName) {
		return getImplementation().createDocument(ns, qualifiedName, null);
	}

	/**
	 * Gets the shared DOM implementation object (required to actually
	 * <i>do</i> anything XML-related), creating it if necessary.
	 * @return the shared DOM implementation object, or <code>null</code> if it
	 *         couldn't be created
	 */
	protected static DOMImplementation getImplementation() {
		if (XMLSaver.impl == null) {
			try {
				XMLSaver.impl = DOMImplementationRegistry.newInstance().
				       getDOMImplementation("XML 3.0");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return XMLSaver.impl;
	}
}