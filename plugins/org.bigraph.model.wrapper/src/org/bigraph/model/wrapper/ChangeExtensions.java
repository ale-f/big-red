package org.bigraph.model.wrapper;

import java.util.ArrayList;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.process.IParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

final class ChangeExtensions {
	private static final ArrayList<IStepExecutor>
			executors = new ArrayList<IStepExecutor>();
	private static final ArrayList<IStepValidator>
			validators = new ArrayList<IStepValidator>();
	
	private static final ArrayList<IParticipant>
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
						Activator.EXTENSION_POINT_CHANGES)) {
			String name = ice.getName();
			if ("executor".equals(name)) {
				IStepExecutor executor = (IStepExecutor)instantiate(ice);
				if (executor != null) {
					executors.add(executor);
					eInstance.addExecutor(executor);
				}
			} else if ("validator".equals(name)) {
				IStepValidator validator = (IStepValidator)instantiate(ice);
				if (validator != null) {
					validators.add(validator);
					eInstance.addValidator(validator);
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
		
		for (IStepExecutor i : executors)
			eInstance.removeExecutor(i);
		for (IStepValidator i : validators)
			eInstance.removeValidator(i);
		
		executors.clear();
		validators.clear();
		
		for (IParticipant i : descriptorParticipants)
			deInstance.removeParticipant(i);
		
		descriptorParticipants.clear();
	}
}
