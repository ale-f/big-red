package dk.itu.big_red.model.load_save;

import org.bigraph.model.savers.Saver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
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
}
