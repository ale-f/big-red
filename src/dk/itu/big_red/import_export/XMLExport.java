package dk.itu.big_red.import_export;

import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.utilities.DOM;
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
	
	protected XMLExport finish() throws ExportFailedException {
		try {
			DOM.write(getOutputStream(), getDocument());
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
			DOM.applyAttributes(e,
				"src", Project.getRelativePath(
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