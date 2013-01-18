package org.bigraph.model.wrapper;

import java.util.ArrayList;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.process.IParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

final class ChangeExtensions {
	public static final String EXTENSION_POINT_CHANGES =
			"org.bigraph.model.wrapper.changes";
	
	private static final ArrayList<IParticipant>
			participants = new ArrayList<IParticipant>(),
			descriptorParticipants = new ArrayList<IParticipant>();
	
	private static final Object instantiate(IConfigurationElement ice) {
		try {
			return ice.createExecutableExtension("class");
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
		return null;
	}
	
	static void init() {
		ExecutorManager eInstance = ExecutorManager.getInstance();
		DescriptorExecutorManager deInstance =
				DescriptorExecutorManager.getInstance();
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		
		for (IConfigurationElement ice :
				registry.getConfigurationElementsFor(
						EXTENSION_POINT_CHANGES)) {
			String name = ice.getName();
			if ("executor".equals(name) || "validator".equals(name)) {
				IParticipant participant = (IParticipant)instantiate(ice);
				if (participant != null) {
					participants.add(participant);
					eInstance.addParticipant(participant);
				}
			} else if ("descriptorExecutor".equals(name) ||
					"descriptorValidator".equals(name)) {
				IParticipant participant = (IParticipant)instantiate(ice);
				if (participant != null) {
					descriptorParticipants.add(participant);
					deInstance.addParticipant(participant);
				}
			}
		}
	}
	
	static void fini() {
		ExecutorManager eInstance = ExecutorManager.getInstance();
		DescriptorExecutorManager deInstance =
				DescriptorExecutorManager.getInstance();
		
		for (IParticipant i : participants)
			eInstance.removeParticipant(i);
		participants.clear();
		
		for (IParticipant i : descriptorParticipants)
			deInstance.removeParticipant(i);
		descriptorParticipants.clear();
	}
}
