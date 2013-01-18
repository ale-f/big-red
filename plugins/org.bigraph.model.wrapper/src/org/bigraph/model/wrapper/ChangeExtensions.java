package org.bigraph.model.wrapper;

import java.util.ArrayList;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

final class ChangeExtensions {
	private static final ArrayList<IStepExecutor>
			executors = new ArrayList<IStepExecutor>();
	private static final ArrayList<IStepValidator>
			validators = new ArrayList<IStepValidator>();
	
	private static final ArrayList<IDescriptorStepExecutor>
			descriptorExecutors = new ArrayList<IDescriptorStepExecutor>();
	private static final ArrayList<IDescriptorStepValidator>
			descriptorValidators = new ArrayList<IDescriptorStepValidator>();
	
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
			} else if ("descriptorExecutor".equals(name)) {
				IDescriptorStepExecutor executor =
						(IDescriptorStepExecutor)instantiate(ice);
				if (executor != null) {
					descriptorExecutors.add(executor);
					deInstance.addParticipant(executor);
				}
			} else if ("descriptorValidator".equals(name)) {
				IDescriptorStepValidator validator =
						(IDescriptorStepValidator)instantiate(ice);
				if (validator != null) {
					descriptorValidators.add(validator);
					deInstance.addParticipant(validator);
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
		
		for (IDescriptorStepExecutor i : descriptorExecutors)
			deInstance.removeParticipant(i);
		for (IDescriptorStepValidator i : descriptorValidators)
			deInstance.removeParticipant(i);
		
		descriptorExecutors.clear();
		descriptorValidators.clear();
	}
}
