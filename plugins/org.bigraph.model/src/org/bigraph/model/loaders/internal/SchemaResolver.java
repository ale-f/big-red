package org.bigraph.model.loaders.internal;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.bigraph.model.resources.IOpenable;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

public final class SchemaResolver implements LSResourceResolver {
	private SchemaResolver() {}
	private static final SchemaResolver INSTANCE = new SchemaResolver();
	
	public static SchemaResolver getInstance() {
		return INSTANCE;
	}
	
	private static final Map<String, IOpenable> schemaOpeners =
			new HashMap<String, IOpenable>();
	
	public static void registerSchema(String namespaceURI, IOpenable of) {
		schemaOpeners.put(namespaceURI, of);
	}
	
	public static void unregisterSchema(String namespaceURI) {
		schemaOpeners.remove(namespaceURI);
	}
	
	@Override
	public LSInput resolveResource(String type, String namespaceURI,
			String publicId, String systemId, String baseURI) {
		System.out.println("SchemaResolver.resolveResource(" + type + ", " + namespaceURI + ", ...)");
		if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type)) {
			IOpenable f = schemaOpeners.get(namespaceURI);
			if (f != null) {
				InputStream is = f.open();
				return (is != null ?
						new Input(is, publicId, systemId, baseURI) : null);
			} else return null;
		} else return null;
	}
}
