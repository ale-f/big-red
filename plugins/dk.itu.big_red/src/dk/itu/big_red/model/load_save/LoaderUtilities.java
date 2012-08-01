package dk.itu.big_red.model.load_save;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.bigraph.model.loaders.RedNamespaceConstants;
import org.bigraph.model.loaders.IXMLUndecorator;
import org.bigraph.model.loaders.XMLLoader;
import org.bigraph.model.resources.IOpenable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.xml.sax.SAXException;

public abstract class LoaderUtilities {
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
	
	public static final String EXTENSION_POINT = "dk.itu.big_red.import";

	public static final String EXTENSION_POINT_XML = "dk.itu.big_red.xml";
	
	private LoaderUtilities() {}
	
	/**
	 * Installs the undecorators registered with the XML extension point into
	 * the given {@link XMLLoader}.
	 * @param l a {@link XMLLoader} to populate with undecorators
	 */
	public static void installUndecorators(XMLLoader l) {
		IExtensionRegistry r = RegistryFactory.getRegistry();
		for (IConfigurationElement ice :
			r.getConfigurationElementsFor(EXTENSION_POINT_XML)) {
			if ("undecorator".equals(ice.getName())) {
				try {
					IXMLUndecorator u = (IXMLUndecorator)
							ice.createExecutableExtension("class");
					l.addUndecorator(u);
				} catch (CoreException e) {
					e.printStackTrace();
					/* do nothing */
				}
			}
		}
	}
	
	/**
	 * Creates a new {@link XMLLoader} and calls {@link
	 * #installUndecorators(XMLLoader)} on it.
	 * @param klass the new loader's {@link Class}
	 * @return a new {@link XMLLoader}, or <code>null</code> if the
	 * instantiation failed for some reason
	 */
	public <T extends XMLLoader> T newLoader(Class<? extends T> klass) {
		try {
			T loader = klass.newInstance();
			installUndecorators(loader);
			return loader;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
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
