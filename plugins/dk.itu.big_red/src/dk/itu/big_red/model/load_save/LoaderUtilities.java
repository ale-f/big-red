package dk.itu.big_red.model.load_save;

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
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
		signature = tryOpenRegister(IRedNamespaceConstants.SIGNATURE,
				"/resources/schema/signature.xsd");
		bigraph = tryOpenRegister(IRedNamespaceConstants.BIGRAPH,
				"/resources/schema/bigraph.xsd");
		rule = tryOpenRegister(IRedNamespaceConstants.RULE,
				"/resources/schema/rule.xsd");
		spec = tryOpenRegister(IRedNamespaceConstants.SPEC,
				"/resources/schema/spec.xsd");
		edit = tryOpenRegister(null, "/resources/schema/edit.xsd");
	}
	
	public static Schema getBigraphSchema() {
		return bigraph;
	}

	public static Schema getSignatureSchema() {
		return signature;
	}

	public static Schema getRuleSchema() {
		return rule;
	}

	public static Schema getSpecSchema() {
		return spec;
	}

	@Deprecated
	public static Schema getEditSchema() {
		return edit;
	}
}
