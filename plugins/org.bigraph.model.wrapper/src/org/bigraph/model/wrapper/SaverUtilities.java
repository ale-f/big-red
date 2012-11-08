package org.bigraph.model.wrapper;

import org.bigraph.model.process.IParticipant;
import org.bigraph.model.process.IParticipantFactory;
import org.bigraph.model.process.IParticipantHost;
import org.bigraph.model.process.ParticipantManager;
import org.bigraph.model.savers.Saver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

public abstract class SaverUtilities {
	private static final class ParticipantContributor
			implements IParticipantFactory {
		private static final ParticipantContributor INSTANCE =
				new ParticipantContributor();
		
		@Override
		public void addParticipants(IParticipantHost host) {
			if (!(host instanceof Saver))
				return;
			IExtensionRegistry r = RegistryFactory.getRegistry();
			for (IConfigurationElement ice :
					r.getConfigurationElementsFor(EXTENSION_POINT)) {
				if ("participant".equals(ice.getName())) {
					try {
						host.addParticipant((IParticipant)
								ice.createExecutableExtension("class"));
					} catch (CoreException e) {
						e.printStackTrace();
						/* do nothing */
					}
				}
			}
		}
	}
	
	static void init() {
		ParticipantManager.getInstance().addFactory(
				ParticipantContributor.INSTANCE);
	}
	
	static void fini() {
		ParticipantManager.getInstance().removeFactory(
				ParticipantContributor.INSTANCE);
	}
	
	private SaverUtilities() {}
	
	public static final String EXTENSION_POINT =
			"org.bigraph.model.wrapper.export";

	public static Saver forContentType(String ct) throws CoreException {
		return forContentType(
				Platform.getContentTypeManager().getContentType(ct));
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
		return s;
	}
}
