package dk.itu.big_red.import_export;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.utilities.resources.IFileBackable;
import dk.itu.big_red.utilities.resources.Project;

public abstract class XMLExport extends Export {
	private Document doc = null;
	
	public Document getDocument() {
		return doc;
	}
	
	public Element getDocumentElement() {
		if (getDocument() != null) {
			return getDocument().getDocumentElement();
		} else return null;
	}
	
	public XMLExport setDocument(Document doc) {
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
	
	private TransformerFactory tf;
	
	protected XMLExport finish() throws ExportFailedException {
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
			throw new ExportFailedException(e);
		}
	}
	
	public abstract Element processObject(Element e, Object object)
		throws ExportFailedException;
	
	protected Element processOrReference(
		Element e, IResource relativeTo, IFileBackable object,
		Class<? extends XMLExport> klass) {
		if (e == null || object == null) {
			return null;
		} else if (object.getFile() != null) {
			e.setAttributeNS(null, "src",
					Project.getRelativePath(
						relativeTo, object.getFile()).toString());	
		} else {
			XMLExport ex;
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
}