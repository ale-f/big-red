package dk.itu.big_red.model.load_save;

import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;

/**
 * Classes extending Saver can write objects to an {@link OutputStream}.
 * @see Loader
 * @author alec
 */
public abstract class Saver extends org.bigraph.model.savers.Saver {
	public static final String EXTENSION_POINT = "dk.itu.big_red.export";
	
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
