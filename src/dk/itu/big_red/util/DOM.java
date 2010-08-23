package dk.itu.big_red.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xml.sax.SAXException;

import dk.itu.big_red.exceptions.ValidationFailedException;

/**
 * Utility functions for manipulating DOM {@link Document}s and
 * {@link Element}s.
 * @author alec
 *
 */
public class DOM {
	private static DOMImplementation impl = null;
	
	/**
	 * Gets the shared DOM implementation object (required to actually
	 * <i>do</i> anything XML-related), creating it if necessary.
	 * @return the shared DOM implementation object, or <code>null</code> if it
	 *         couldn't be created
	 */
	public static DOMImplementation getImplementation() {
		if (impl == null) {
			try {
				impl = DOMImplementationRegistry.newInstance().
				       getDOMImplementation("XML 3.0");
			} catch (ClassCastException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
		}
		return impl;
	}
	
	/**
	 * Attempts to parse the specified {@link InputStream} into a DOM {@link
	 * Document}.
	 * @param is an InputStream
	 * @return a Document
	 * @throws SAXException as {@link DocumentBuilder#parse(File)}
	 * @throws CoreException as {@link IFile#getContents()}
	 * @throws IOException as {@link DocumentBuilder#parse(File)} or {@link InputStream#close}
	 * @throws ParserConfigurationException as {@link DocumentBuilderFactory#newDocumentBuilder()}
	 */
	public static Document parse(InputStream is) throws SAXException, CoreException, IOException, ParserConfigurationException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			return dbf.newDocumentBuilder().parse(is);
		} finally {
			is.close();
		}
	}
	
	/**
	 * Converts the specified {@link Document} into a textual representation of
	 * a XML document, then writes it to the specified {@link OutputStream}.
	 * @param os an OutputStream
	 * @param d a Document
	 * @throws CoreException if the file couldn't be overwritten
	 * @throws TransformerException if the Node couldn't be converted to XML
	 */
	public static void write(OutputStream os, Document d) throws CoreException, TransformerException {
		TransformerFactory f = TransformerFactory.newInstance();
		
		Source source = new DOMSource(d);
		Result result = new StreamResult(os);
		
		Transformer t = f.newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.transform(source, result);
	}
	
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
			SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
				newSchema(new StreamSource(schema)).newValidator().validate(
					new DOMSource(d));
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
	 */
	public static void applyAttributesToElement(Element d, Object... attrs) {
		for (int i = 0; i < attrs.length; i += 2)
			d.setAttribute(attrs[i].toString(), attrs[i + 1].toString());
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
	public static ArrayList<Element> getNamedChildElements(Element d, String nsURI, String n) {
		ArrayList<Element> r = new ArrayList<Element>();
		NodeList p = d.getChildNodes();
		for (int i = 0; i < p.getLength(); i++) {
			Node t = p.item(i);
			if (t instanceof Element &&
					t.getNamespaceURI().equals(nsURI) &&
					t.getLocalName().equals(n))
				r.add((Element)t);
		}
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
	 * Retrieves the given named attribute from the given Element. (Either the
	 * attribute or the Element must have the given namespace; see Neil
	 * Bradley's <q>The XML Companion</q>, third edition, page 160 for why this
	 * is necessary.)
	 * @param d an Element with attributes set
	 * @param nsURI the attribute (or Element's) namespace
	 * @param n the attribute name to search for
	 * @return the attribute's value
	 */
	public static String getAttributeNS(Element d, String nsURI, String n) {
		String r = d.getAttributeNS(nsURI, n);
		if ((r == null || r.length() == 0) && d.getNamespaceURI().equals(nsURI)) {
			r = d.getAttributeNS(null, n);
		}
		return r;
	}
	
	public static int getIntAttribute(Element d, String nsURI, String n) {
		try {
			String attr = getAttributeNS(d, nsURI, n);
			System.out.println("getIntAttribute(" + d + ", \"" + nsURI + "\", \"" + n + "\")");
			System.out.println("attr is " + attr);
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
	 * automatically converting the result from a string into a {@link RGB}
	 * colour.
	 * @param d an Element with attributes set
	 * @param nsURI the attribute's namespace
	 * @param n the attribute name to search for
	 * @return the attribute's value as a RGB colour, or <code>null</code> if
	 *         the attribute couldn't be found
	 * @see DOM#getAttribute
	 */
	public static RGB getColorAttribute(Element d, String nsURI, String n) {
		return Utility.colourFromString(getAttributeNS(d, nsURI, n));
	}
	
	/**
	 * Appends <code>newChild</code> to <code>e</code>, if neither of them are
	 * <code>null</code>.
	 * @param e the would-be parent of the new node
	 * @param newChild the node to add
	 */
	public static void appendChildIfNotNull(Element e, Node newChild) {
		if (e != null && newChild != null)
			e.appendChild(newChild);
	}
	
	public static boolean nameEqualsNS(Element e, String nsURI, String nodeName) {
		return (e.getNamespaceURI().equals(nsURI) && e.getLocalName().equals(nodeName));
	}
}
