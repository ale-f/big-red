package org.bigraph.model.loaders;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bigraph.model.ModelObject;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.loaders.internal.SchemaResolver;
import org.bigraph.model.resources.IFileWrapper;
import org.bigraph.model.resources.IOpenable;
import org.bigraph.model.resources.IResourceWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class XMLLoader extends ChangeLoader implements IXMLLoader {
	public XMLLoader() {
	}
	
	public XMLLoader(Loader parent) {
		super(parent);
		if (parent instanceof XMLLoader)
			addNewUndecorators(((XMLLoader)parent).getUndecorators());
	}
	
	private static final SchemaFactory sf;
	private static final DocumentBuilderFactory dbf;
	private static final DocumentBuilder db;
	static {
		sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		sf.setResourceResolver(SchemaResolver.getInstance());
		
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db_ = null;
		try {
			db_ = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException pce) {
			/* do nothing */
		}
		db = db_;
	}
	
	public static SchemaFactory getSharedSchemaFactory() {
		return sf;
	}
	
	public static DocumentBuilder getSharedDocumentBuilder() {
		return db;
	}
	
	public static void registerSchema(String namespaceURI, IOpenable of) {
		SchemaResolver.getInstance().registerSchema(namespaceURI, of);
	}
	
	public static void unregisterSchema(String namespaceURI) {
		SchemaResolver.getInstance().unregisterSchema(namespaceURI);
	}
	
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

	public static double getDoubleAttribute(
			Element d, String nsURI, String n) {
		try {
			return Double.parseDouble(getAttributeNS(d, nsURI, n));
		} catch (Exception e) {
			return 0;
		}
	}
	
	protected static <T extends Node> T validate(T d, Schema schema)
			throws LoadFailedException {
		try {
			schema.newValidator().validate(new DOMSource(d));
			return d;
		} catch (SAXException e) {
			throw new LoadFailedException(e);
		} catch (IOException e) {
			throw new LoadFailedException(e);
		}
	}
	
	/**
	 * Attempts to parse the specified {@link InputStream} into a DOM {@link
	 * Document}.
	 * @param is an InputStream, which will be closed &mdash; even in the
	 * event of an exception
	 * @return a Document
	 * @throws SAXException as {@link DocumentBuilder#parse(File)}
	 * @throws IOException as {@link DocumentBuilder#parse(File)} or
	 * {@link InputStream#close}
	 */
	protected static Document parse(InputStream is)
			throws SAXException, IOException {
		try {
			return getSharedDocumentBuilder().parse(is);
		} finally {
			is.close();
		}
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
	 * Gets all the children of the specified element with the given name and
	 * namespace.
	 * (Note that this method only searches immediate children.)
	 * @param d an Element containing children
	 * @param nsURI the namespace to search in
	 * @param n the tag name to search for
	 * @return an ArrayList of child elements
	 */
	protected static ArrayList<Element> getNamedChildElements(
			Element d, String ns, String n) {
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
	 * @see XMLLoader#getNamedChildElements
	 */
	protected static Element getNamedChildElement(
			Element d, String nsURI, String n) {
		ArrayList<Element> r = getNamedChildElements(d, nsURI, n);
		if (r.size() == 1)
			return r.get(0);
		else return null;
	}
	
	protected <T extends ModelObject> T loadRelative(String replacement,
			Class<? extends T> klass, XMLLoader loader)
					throws LoadFailedException {
		if (replacement != null) {
			if (getFile() == null)
				 throw new Error("BUG: relative path to resolve, " +
							"but no IFileWrapper set on " + this);
			IResourceWrapper rw =
					getFile().getParent().getResource(replacement);
			if (rw instanceof IFileWrapper) {
				IFileWrapper fw = (IFileWrapper)rw;
				loader.setFile(fw).setInputStream(fw.getContents());
				ModelObject mo = loader.importObject();
				if (klass.isInstance(mo)) {
					return klass.cast(mo);
				} else throw new LoadFailedException(
						"Referenced document is not of the correct type");
			} else throw new LoadFailedException(
					"Referenced document is not valid");
		} else return null;
	}
	
	protected abstract ModelObject makeObject(Element el)
			throws LoadFailedException;
	
	protected static <T> T selectFirst(T... objects) {
		for (T i : objects)
			if (i != null)
				return i;
		return null;
	}
	
	protected <T extends ModelObject> T loadSub(
			Element el, String myNS, Class<T> klass, XMLLoader loader)
			throws LoadFailedException {
		if (el == null)
			return null;
		loader.setFile(getFile());
		String
			src = getAttributeNS(el, myNS, "src"),
			theirNS = el.getNamespaceURI();
		/* If this element is foreign, or if it doesn't have a src attribute,
		 * then let the loader do everything */
		if (!(myNS != null ? myNS.equals(theirNS) : theirNS == null) ||
				src == null) {
			try {
				return klass.cast(loader.makeObject(el));
			} catch (ClassCastException cce) {
				throw new LoadFailedException(cce);
			}
		/* Otherwise, interpret the src attribute (which can't be null) */
		} else return loadRelative(src, klass, loader);
	}
	
	private List<IXMLUndecorator> undecorators = null;

	protected List<IXMLUndecorator> getUndecorators() {
		return (undecorators != null ? undecorators :
				Collections.<IXMLUndecorator>emptyList());
	}

	public void addUndecorator(IXMLUndecorator one) {
		if (one == null)
			return;
		if (undecorators == null)
			undecorators = new ArrayList<IXMLUndecorator>();
		undecorators.add(one);
		one.setLoader(this);
	}

	public XMLLoader addNewUndecorators(
			Collection<? extends IXMLUndecorator> many) {
		if (many != null)
			for (IXMLUndecorator i : many)
				addUndecorator(i.newInstance());
		return this;
	}
	
	protected <T extends ModelObject> T executeUndecorators(T mo, Element el) {
		if (mo != null && el != null)
			for (IXMLUndecorator d : getUndecorators())
				d.undecorate(mo, el);
		return mo;
	}

	@Override
	protected void executeChanges(IChangeExecutor ex)
			throws LoadFailedException {
		for (IXMLUndecorator d : getUndecorators())
			d.finish(ex);
		super.executeChanges(ex);
	}
}
