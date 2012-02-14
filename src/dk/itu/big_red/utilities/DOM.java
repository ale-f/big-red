package dk.itu.big_red.utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Utility functions for manipulating DOM {@link Document}s and
 * {@link Element}s.
 * @author alec
 *
 */
public class DOM {
	private static DocumentBuilderFactory dbf = null;
	
	/**
	 * Attempts to parse the specified {@link InputStream} into a DOM {@link
	 * Document}.
	 * @param is an InputStream, which will be closed &mdash; even in the
	 * event of an exception
	 * @return a Document
	 * @throws SAXException as {@link DocumentBuilder#parse(File)}
	 * @throws CoreException as {@link IFile#getContents()}
	 * @throws IOException as {@link DocumentBuilder#parse(File)} or {@link InputStream#close}
	 * @throws ParserConfigurationException as {@link DocumentBuilderFactory#newDocumentBuilder()}
	 */
	public static Document parse(InputStream is) throws SAXException, CoreException, IOException, ParserConfigurationException {
		try {
			if (dbf == null) {
				dbf = DocumentBuilderFactory.newInstance();
				dbf.setNamespaceAware(true);
			}
			return dbf.newDocumentBuilder().parse(is);
		} finally {
			is.close();
		}
	}
	
	private static SchemaFactory sf = null;
	
	/**
	 * Validates the given {@link Document} with the {@link Schema} constructed
	 * from the given {@link InputStream}.
	 * @param d a Document
	 * @param schema an InputStream
	 * @return <code>d</code>, for convenience
	 * @throws ValidationFailedException if the validation (or the validator's
	 *         initialisation and configuration) failed
	 */
	public static Document validate(Document d, InputStream schema) throws ValidationFailedException {
		try {
			if (sf == null)
				sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			sf.newSchema(new StreamSource(schema)).newValidator().
				validate(new DOMSource(d));
			return d;
		} catch (Exception e) {
			throw new ValidationFailedException(e);
		}
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
	 * Gets all the children of the specified element with the given name and
	 * namespace.
	 * (Note that this method only searches immediate children.)
	 * @param d an Element containing children
	 * @param nsURI the namespace to search in
	 * @param n the tag name to search for
	 * @return an ArrayList of child elements
	 */
	public static ArrayList<Element> getNamedChildElements(Element d, String ns, String n) {
		ArrayList<Element> r = new ArrayList<Element>();
		for (Element t : getChildElements(d))
			if (t.getNamespaceURI().equals(ns) && t.getLocalName().equals(n))
				r.add(t);
		return r;
	}

	/**
	 * Returns the unique child of the specified Element which has the given
	 * tag name.
	 * @param d an Element containing children
	 * @param n the tag name to search for
	 * @return the unique named child, or <code>null</code> if there were zero
	 *         or more than one matches
	 * @see DOM#getNamedChildElements
	 */
	public static Element getNamedChildElement(Element d, String nsURI, String n) {
		ArrayList<Element> r = getNamedChildElements(d, nsURI, n);
		if (r.size() == 1)
			return r.get(0);
		else return null;
	}

	/**
	 * Removes the unique child of the specified Element which has the given
	 * tag name, and returns it.
	 * @param d an Element containing children
	 * @param n the tag name to search for
	 * @return the unique named (former) child, or <code>null</code> if there
	 *         were zero or more than one matches
	 */
	public static Element removeNamedChildElement(Element d, String nsURI, String n) {
		Element r = getNamedChildElement(d, nsURI, n);
		if (r != null)
			r.getParentNode().removeChild(r);
		return r;
	}

	/**
	 * Returns all the child {@link Node}s of the specified {@link Element}
	 * which are themselves {@link Element}s.
	 * @param e an Element containing children
	 * @return a list of child {@link Element}s
	 */
	public static List<Element> getChildElements(Element e) {
		ArrayList<Element> children = new ArrayList<Element>();
		int length = e.getChildNodes().getLength();
		for (int h = 0; h < length; h++) {
			Node i = e.getChildNodes().item(h);
			if (i instanceof Element)
				children.add((Element)i);
		}
		return children;
	}
	
	/**
	 * Retrieves the given named attribute from the given Element. (Either the
	 * attribute or the Element must have the given namespace; see Neil
	 * Bradley's <q>The XML Companion</q>, third edition, page 160 for why this
	 * is necessary.)
	 * @param d an Element with attributes set
	 * @param nsURI the attribute (or Element's) namespace
	 * @param n the attribute name to search for
	 * @return the attribute's value, or <code>null</code> if it wasn't present
	 */
	public static String getAttributeNS(Element d, String nsURI, String n) {
		String r = d.getAttributeNS(nsURI, n);
		if (r.length() == 0 && d.getNamespaceURI().equals(nsURI))
			r = d.getAttributeNS(null, n);
		return (r.length() != 0 ? r : null);
	}
	
	public static int getIntAttribute(Element d, String nsURI, String n) {
		try {
			return Integer.parseInt(getAttributeNS(d, nsURI, n));
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Retrieves the given named attribute from the specified Element,
	 * automatically converting the result from a string into a double.
	 * @param d an Element with attributes set
	 * @param nsURI the attribute's namespace
	 * @param n the attribute name to search for
	 * @return the attribute's value as an double, or <code>0</code> if the
	 *         attribute couldn't be found
	 * @see DOM#getAttribute
	 */
	public static double getDoubleAttribute(Element d, String nsURI, String n) {
		try {
			return Double.parseDouble(getAttributeNS(d, nsURI, n));
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Retrieves the given named attribute from the specified Element,
	 * automatically converting the result from a string into a {@link Colour}.
	 * @param d an Element with attributes set
	 * @param nsURI the attribute's namespace
	 * @param n the attribute name to search for
	 * @return the attribute's value as a {@link Colour}, or <code>null</code>
	 *         if the attribute couldn't be found
	 * @see DOM#getAttribute
	 */
	public static Colour getColorAttribute(Element d, String nsURI, String n) {
		return new Colour(getAttributeNS(d, nsURI, n));
	}
	
	public static Iterable<Node> iterableChildren(Node n) {
		if (n != null) {
			return iterable(n.getChildNodes());
		} else return null;
	}
	
	public static Iterable<Node> iterable(final NodeList nl_) {
		if (nl_ != null) {
			return new IterableWrapper<Node>() {
				@Override
				protected Node item(int index) {
					return nl_.item(index);
				}

				@Override
				protected int count() {
					return nl_.getLength();
				}
			};
		} else return null;
	}
}
