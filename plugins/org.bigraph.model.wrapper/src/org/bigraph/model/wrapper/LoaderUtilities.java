package org.bigraph.model.wrapper;

import org.bigraph.model.loaders.Loader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

public abstract class LoaderUtilities {
	private static final class Holder {
		private static final EclipseParticipantFactory FACTORY =
				new EclipseParticipantFactory(EXTENSION_POINT);
	}
	
	static void init() {
		Loader.getParticipantManager().addFactory(Holder.FACTORY);
	}
	
	static void fini() {
		Loader.getParticipantManager().removeFactory(Holder.FACTORY);
	}
	
	private LoaderUtilities() {}
	
	public static final String EXTENSION_POINT =
			"org.bigraph.model.wrapper.import";
	
	public static Loader forContentType(String ct) throws CoreException {
		return forContentType(
				Platform.getContentTypeManager().getContentType(ct));
	}
	
	/**
	 * Creates and returns a new {@link Loader} for the given content type.
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
		return l;
	}
}
