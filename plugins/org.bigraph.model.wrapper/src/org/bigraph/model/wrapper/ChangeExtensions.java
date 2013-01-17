package org.bigraph.model.wrapper;

import java.util.ArrayList;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;

final class ChangeExtensions {
	private static final ArrayList<IStepExecutor> executors =
			new ArrayList<IStepExecutor>();
	private static final ArrayList<IStepValidator> validators =
			new ArrayList<IStepValidator>();
	
	static void init() {
		ExecutorManager eInstance = ExecutorManager.getInstance();
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		
		for (IConfigurationElement ice :
				registry.getConfigurationElementsFor(
						Activator.EXTENSION_POINT_CHANGES)) {
			String name = ice.getName();
			if ("executor".equals(name)) {
				IStepExecutor executor;
				try {
					executor = (IStepExecutor)
							ice.createExecutableExtension("class");
				} catch (CoreException ce) {
					ce.printStackTrace();
					continue;
				}
				executors.add(executor);
				eInstance.addExecutor(executor);
			} else if ("validator".equals(name)) {
				IStepValidator validator;
				try {
					validator = (IStepValidator)
							ice.createExecutableExtension("class");
				} catch (CoreException ce) {
					ce.printStackTrace();
					continue;
				}
				validators.add(validator);
				eInstance.addValidator(validator);
			}
		}
	}
	
	static void fini() {
		ExecutorManager eInstance = ExecutorManager.getInstance();
		for (IStepExecutor i : executors)
			eInstance.removeExecutor(i);
		for (IStepValidator i : validators)
			eInstance.removeValidator(i);
		
		executors.clear();
		validators.clear();
	}
}
