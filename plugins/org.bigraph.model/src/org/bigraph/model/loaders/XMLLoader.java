package org.bigraph.model.loaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.SchemaFactory;

import org.bigraph.model.ModelObject;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.loaders.internal.SchemaResolver;
import org.bigraph.model.resources.IOpenable;
import org.w3c.dom.Element;

public abstract class XMLLoader extends ChangeLoader implements IXMLLoader {
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
	
	protected static SchemaFactory getSharedSchemaFactory() {
		return sf;
	}
	
	protected static DocumentBuilder getSharedDocumentBuilder() {
		return db;
	}
	
	public static void registerSchema(String namespaceURI, IOpenable of) {
		SchemaResolver.registerSchema(namespaceURI, of);
	}
	
	public static void unregisterSchema(String namespaceURI) {
		SchemaResolver.unregisterSchema(namespaceURI);
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

	private List<IXMLUndecorator> undecorators = null;

	protected List<IXMLUndecorator> getUndecorators() {
		return (undecorators != null ? undecorators :
				Collections.<IXMLUndecorator>emptyList());
	}

	public void addUndecorator(IXMLUndecorator d) {
		if (d == null)
			return;
		if (undecorators == null)
			undecorators = new ArrayList<IXMLUndecorator>();
		undecorators.add(d);
		d.setLoader(this);
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
