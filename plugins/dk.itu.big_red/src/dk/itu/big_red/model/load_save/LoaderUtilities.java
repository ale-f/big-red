package dk.itu.big_red.model.load_save;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.bigraph.model.loaders.Loader;
import org.bigraph.model.loaders.RedNamespaceConstants;
import org.bigraph.model.loaders.IXMLUndecorator;
import org.bigraph.model.loaders.XMLLoader;
import org.bigraph.model.resources.IOpenable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;
import org.xml.sax.SAXException;

import dk.itu.big_red.utilities.resources.Project;

public abstract class LoaderUtilities {
	private LoaderUtilities() {}
	
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
	
	public static Loader forContentType(String ct) throws CoreException {
		return forContentType(
				Project.getContentTypeManager().getContentType(ct));
	}
	
	/**
	 * Creates and returns a new {@link Loader} for the given content type.
	 * <p>If the {@link Loader} is a {@link XMLLoader}, then this method will
	 * automatically call {@link #installUndecorators(XMLLoader)}.
	 * @param ct an {@link IContentType}
	 * @return a {@link Loader}, or <code>null</code> if one couldn't be found
	 * for the given content type
	 * @throws CoreException as {@link
	 * IConfigurationElement#createExecutableExtension(String)}
	 */
	public static Loader forContentType(IContentType ct) throws CoreException {
		Loader l = null;
		for (IConfigurationElement ice :
				RegistryFactory.getRegistry().
				getConfigurationElementsFor(EXTENSION_POINT)) {
			if (ct.getId().equals(ice.getAttribute("contentType"))) {
				l = (Loader)ice.createExecutableExtension("class");
				break;
			}
		}
		if (l instanceof XMLLoader)
			installUndecorators((XMLLoader)l);
		return l;
	}
}
