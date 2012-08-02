package dk.itu.big_red.model.load_save;

import javax.xml.validation.Schema;

import org.bigraph.model.loaders.Loader;
import org.bigraph.model.loaders.IXMLUndecorator;
import org.bigraph.model.loaders.Schemas;
import org.bigraph.model.loaders.XMLLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;
import dk.itu.big_red.utilities.resources.Project;

public abstract class LoaderUtilities {
	private LoaderUtilities() {}
	
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
