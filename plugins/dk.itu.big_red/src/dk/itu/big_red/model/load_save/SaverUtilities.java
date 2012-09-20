package dk.itu.big_red.model.load_save;

import org.bigraph.model.savers.ISaver;
import org.bigraph.model.savers.Saver;
import org.bigraph.model.savers.ISaver.Participant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.core.runtime.content.IContentType;

import dk.itu.big_red.utilities.resources.Project;

public abstract class SaverUtilities {
	private static final class ParticipantContributor
			implements ISaver.InheritableParticipant {
		@Override
		public void setSaver(ISaver saver) {
			IExtensionRegistry r = RegistryFactory.getRegistry();
			for (IConfigurationElement ice :
					r.getConfigurationElementsFor(EXTENSION_POINT)) {
				if ("participant".equals(ice.getName())) {
					try {
						saver.addParticipant((ISaver.Participant)
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
	
	private SaverUtilities() {}
	
	public static final String EXTENSION_POINT = "dk.itu.big_red.export";

	public static void installParticipants(Saver saver) {
		if (saver != null)
			saver.addParticipant(new ParticipantContributor());
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
		installParticipants(s);
		return s;
	}
}
