package dk.itu.big_red.model.load_save;

import org.bigraph.model.loaders.IXMLUndecorator;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.XMLLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

public abstract class LoaderUtilities {
	public static final String EXTENSION_POINT = "dk.itu.big_red.import";

	private LoaderUtilities() {}
	
	public static void installUndecorators(XMLLoader l) {
		IExtensionRegistry r = RegistryFactory.getRegistry();
		for (IConfigurationElement ice :
			r.getConfigurationElementsFor(EXTENSION_POINT)) {
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
	
	protected static <T extends XMLLoader> T newLoader(Class<T> klass)
			throws LoadFailedException {
		T l;
		try {
			l = klass.newInstance();
		} catch (IllegalAccessException iae) {
			throw new LoadFailedException(iae);
		} catch (InstantiationException ie) {
			throw new LoadFailedException(ie);
		}
		installUndecorators(l);
		return l;
	}
}
