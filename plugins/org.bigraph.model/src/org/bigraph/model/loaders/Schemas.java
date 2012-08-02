package org.bigraph.model.loaders;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.bigraph.model.resources.IOpenable;
import org.xml.sax.SAXException;

public abstract class Schemas {
	private static final class ResourceOpenable implements IOpenable {
		private final String path;
		
		private ResourceOpenable(String path) {
			this.path = path;
		}
		
		@Override
		public InputStream open() {
			return getClass().getResourceAsStream(path);
		}
	}
	
	private Schemas() {}
	
	private static Schema tryOpenRegister(String ns, String path) {
		ResourceOpenable file = new ResourceOpenable(path);
		try {
			Schema s = XMLLoader.getSharedSchemaFactory().newSchema(
					new StreamSource(file.open()));
			if (ns != null)
				XMLLoader.registerSchema(ns, file);
			return s;
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static final Schema
		bigraph, signature, rule, spec, edit;
	static {
		signature = tryOpenRegister(RedNamespaceConstants.SIGNATURE,
				"/resources/schema/signature.xsd");
		bigraph = tryOpenRegister(RedNamespaceConstants.BIGRAPH,
				"/resources/schema/bigraph.xsd");
		rule = tryOpenRegister(RedNamespaceConstants.RULE,
				"/resources/schema/rule.xsd");
		spec = tryOpenRegister(RedNamespaceConstants.SPEC,
				"/resources/schema/spec.xsd");
		edit = tryOpenRegister(null, "/resources/schema/edit.xsd");
	}
	
	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>bigraph</code> documents.
	 * @return a {@link Schema}
	 */
	public static Schema getBigraphSchema() {
		return bigraph;
	}

	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>signature</code> documents.
	 * @return a {@link Schema}
	 */
	public static Schema getSignatureSchema() {
		return signature;
	}

	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>rule</code> documents.
	 * @return a {@link Schema}
	 */
	public static Schema getRuleSchema() {
		return rule;
	}

	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>spec</code> documents.
	 * @return a {@link Schema}
	 */
	public static Schema getSpecSchema() {
		return spec;
	}

	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>edit</code> documents.
	 * @return a {@link Schema}
	 * @deprecated This method may go away at any point.
	 */
	@Deprecated
	public static Schema getEditSchema() {
		return edit;
	}
}
