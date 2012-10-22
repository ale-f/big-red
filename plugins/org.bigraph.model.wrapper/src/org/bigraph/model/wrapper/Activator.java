package org.bigraph.model.wrapper;

import java.util.ArrayList;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.ValidatorManager;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.BundleContext;

public final class Activator extends Plugin {
	private static Activator instance;
	
	private ArrayList<IStepExecutor> executors;
	private ArrayList<IStepValidator> validators;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		
		install();
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		uninstall();
		
		instance = null;
		super.stop(context);
	}
	
	public static Activator getInstance() {
		return instance;
	}
	
	public static final String EXTENSION_POINT_CHANGES =
			"org.bigraph.model.wrapper.changes";
	
	private void install() {
		executors = new ArrayList<IStepExecutor>();
		validators = new ArrayList<IStepValidator>();
		
		ExecutorManager eInstance = ExecutorManager.getInstance();
		ValidatorManager vInstance = ValidatorManager.getInstance();
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		
		for (IConfigurationElement ice :
				registry.getConfigurationElementsFor(
						EXTENSION_POINT_CHANGES)) {
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
				vInstance.addValidator(validator);
			}
		}
	}
	
	private void uninstall() {
		ValidatorManager vInstance = ValidatorManager.getInstance();
		for (IStepValidator i : validators)
			vInstance.removeValidator(i);
		
		ExecutorManager eInstance = ExecutorManager.getInstance();
		for (IStepExecutor i : executors)
			eInstance.removeExecutor(i);
		
		executors.clear();
		validators.clear();
		
		executors = null;
		validators = null;
	}
}
