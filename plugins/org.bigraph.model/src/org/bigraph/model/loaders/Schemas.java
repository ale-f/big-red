package org.bigraph.model.loaders;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.bigraph.model.loaders.internal.SchemaResolver;
import org.bigraph.model.resources.ResourceOpenable;
import org.xml.sax.SAXException;

public abstract class Schemas {
	private static final SchemaFactory sf;
	static {
		sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		sf.setResourceResolver(SchemaResolver.getInstance());
	}
	
	private static final class SchemaSpec {
		private final String xmlns, path;
		private ResourceOpenable file;
		
		public SchemaSpec(String xmlns, String path) {
			this.xmlns = xmlns;
			this.path = path;
			SchemaResolver.getInstance().registerSchema(
					xmlns, file = new ResourceOpenable(Schemas.class, path));
		}
		
		private Schema schema;
		
		private Schema getSchema() {
			if (schema == null) {
				synchronized (this) {
					try {
						if (schema == null)
							schema = sf.newSchema(
									new StreamSource(file.open()));
					} catch (SAXException e) {
						throw new RuntimeException(
								"BUG: internal schema " + this +
								" couldn't be loaded", e);
					}
				}
			}
			return schema;
		}
		
		@Override
		public String toString() {
			return "SchemaSpec(" + xmlns + ", " + path + ")";
		}
	}
	
	private static final SchemaSpec
		signature = new SchemaSpec(
				RedNamespaceConstants.SIGNATURE,
				"/resources/schema/signature.xsd"),
		bigraph = new SchemaSpec(
				RedNamespaceConstants.BIGRAPH,
				"/resources/schema/bigraph.xsd"),
		edit = new SchemaSpec(
				RedNamespaceConstants.EDIT,
				"/resources/schema/edit.xsd"),
		rule = new SchemaSpec(
				RedNamespaceConstants.RULE,
				"/resources/schema/rule.xsd"),
		spec = new SchemaSpec(
				RedNamespaceConstants.SPEC,
				"/resources/schema/spec.xsd");
	
	static {
		new SchemaSpec(
				RedNamespaceConstants.EDIT_BIG,
				"/resources/schema/edit-big.xsd");
		new SchemaSpec(
				RedNamespaceConstants.EDIT_SIG,
				"/resources/schema/edit-sig.xsd");
	}
	
	private Schemas() {}
	
	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>bigraph</code> documents.
	 * @return a {@link Schema}
	 */
	public static Schema getBigraphSchema() {
		return bigraph.getSchema();
	}

	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>signature</code> documents.
	 * @return a {@link Schema}
	 */
	public static Schema getSignatureSchema() {
		return signature.getSchema();
	}

	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>rule</code> documents.
	 * @return a {@link Schema}
	 */
	public static Schema getRuleSchema() {
		return rule.getSchema();
	}

	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>spec</code> documents.
	 * @return a {@link Schema}
	 */
	public static Schema getSpecSchema() {
		return spec.getSchema();
	}

	/**
	 * Returns the shared {@link Schema} suitable for validating
	 * <code>edit</code> documents.
	 * @return a {@link Schema}
	 */
	public static Schema getEditSchema() {
		return edit.getSchema();
	}
}
