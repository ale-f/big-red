package dk.itu.big_red.model.load_save;

import org.bigraph.model.savers.IXMLDecorator;
import org.bigraph.model.savers.Saver;
import org.bigraph.model.savers.XMLSaver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

import dk.itu.big_red.utilities.resources.Project;

public abstract class SaverUtilities {
	private SaverUtilities() {}
	
	public static final String EXTENSION_POINT = "dk.itu.big_red.export";

	public static void installDecorators(XMLSaver saver) {
		IExtensionRegistry r = RegistryFactory.getRegistry();
		for (IConfigurationElement ice :
			r.getConfigurationElementsFor(LoaderUtilities.EXTENSION_POINT_XML)) {
			if ("decorator".equals(ice.getName())) {
				try {
					saver.addDecorator(
						(IXMLDecorator)ice.createExecutableExtension("class"));
				} catch (CoreException e) {
					e.printStackTrace();
					/* do nothing */
				}
			}
		}
	}
	
	public static Saver forContentType(String ct) throws CoreException {
		return forContentType(
				Project.getContentTypeManager().getContentType(ct));
	}
	
	public static Saver forContentType(IContentType ct) throws CoreException {
		Saver s = null;
		for (IConfigurationElement ice :
			RegistryFactory.getRegistry().
				getConfigurationElementsFor(EXTENSION_POINT)) {
			if (ct.getId().equals(ice.getAttribute("contentType"))) {
				s = (Saver)ice.createExecutableExtension("class");
				break;
			}
		}
		if (s instanceof XMLSaver)
			installDecorators((XMLSaver)s);
		return s;
	}
}
