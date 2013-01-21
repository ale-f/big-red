package org.bigraph.model.wrapper;

import org.bigraph.model.savers.Saver;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

public abstract class SaverUtilities {
	private static final class Holder {
		private static final EclipseParticipantFactory FACTORY =
				new EclipseParticipantFactory(EXTENSION_POINT);
	}
	
	static void init() {
		Saver.getParticipantManager().addFactory(Holder.FACTORY);
	}
	
	static void fini() {
		Saver.getParticipantManager().removeFactory(Holder.FACTORY);
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
