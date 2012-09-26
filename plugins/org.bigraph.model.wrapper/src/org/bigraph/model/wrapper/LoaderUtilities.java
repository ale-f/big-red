package org.bigraph.model.wrapper;

import org.bigraph.model.loaders.ILoader;
import org.bigraph.model.loaders.ILoader.Participant;
import org.bigraph.model.loaders.Loader;
import org.bigraph.model.loaders.XMLLoader;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

public abstract class LoaderUtilities {
	private static final class ParticipantContributor
			implements ILoader.InheritableParticipant {
		@Override
		public void setLoader(ILoader loader) {
			IExtensionRegistry r = RegistryFactory.getRegistry();
			for (IConfigurationElement ice :
					r.getConfigurationElementsFor(EXTENSION_POINT)) {
				if ("participant".equals(ice.getName())) {
					try {
						loader.addParticipant((ILoader.Participant)
								ice.createExecutableExtension("class"));
					} catch (CoreException e) {
						e.printStackTrace();
						/* do nothing */
					}
				}
			}
		}

		@Override
		public Participant newInstance() {
			return new ParticipantContributor();
		}
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
		if (l != null)
			l.addParticipant(new ParticipantContributor());
		return l;
	}
}
