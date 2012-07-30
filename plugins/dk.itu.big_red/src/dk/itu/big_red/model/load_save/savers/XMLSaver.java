package dk.itu.big_red.model.load_save.savers;

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
	
	public abstract Element processModel(Element e) throws SaveFailedException;
	
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
				ex.setDocument(getDocument()).
						setModel(object).setFile(getFile());
				ex.processModel(e);
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