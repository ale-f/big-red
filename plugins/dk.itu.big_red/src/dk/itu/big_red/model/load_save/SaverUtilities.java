package dk.itu.big_red.model.load_save;

import org.bigraph.model.savers.IXMLDecorator;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.Saver;
import org.bigraph.model.savers.XMLSaver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;


public abstract class SaverUtilities {
	public static final String EXTENSION_POINT = "dk.itu.big_red.export";

	private SaverUtilities() {}

	public static final Saver forContentType(String contentType) {
		for (IConfigurationElement ice :
			RegistryFactory.getRegistry().
				getConfigurationElementsFor(EXTENSION_POINT)) {
			if (contentType.equals(ice.getAttribute("contentType"))) {
				try {
					return (Saver)ice.createExecutableExtension("class");
				} catch (CoreException e) {
					return null;
				}
			}
		}
		return null;
	}

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
	
	protected static <T extends XMLSaver> T newSaver(Class<T> klass)
			throws SaveFailedException {
		T s;
		try {
			s = klass.newInstance();
		} catch (IllegalAccessException iae) {
			throw new SaveFailedException(iae);
		} catch (InstantiationException ie) {
			throw new SaveFailedException(ie);
		}
		installDecorators(s);
		return s;
	}
}
