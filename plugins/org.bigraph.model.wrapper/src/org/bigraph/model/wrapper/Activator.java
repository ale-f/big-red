package org.bigraph.model.wrapper;

import java.util.ArrayList;

import org.bigraph.model.assistants.ValidatorManager;
import org.bigraph.model.changes.IStepValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.BundleContext;

public final class Activator extends Plugin {
	private static Activator instance;
	
	private ArrayList<IStepValidator> validators;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		
		installValidators();
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		uninstallValidators();
		
		instance = null;
		super.stop(context);
	}
	
	public static Activator getInstance() {
		return instance;
	}
	
	public static final String EXTENSION_POINT_VALIDATION =
			"org.bigraph.model.wrapper.validation";
	
	private void installValidators() {
		validators = new ArrayList<IStepValidator>();
		
		ValidatorManager instance = ValidatorManager.getInstance();
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		for (IConfigurationElement ice :
				registry.getConfigurationElementsFor(
						EXTENSION_POINT_VALIDATION)) {
			IStepValidator validator;
			try {
				validator = (IStepValidator)
						ice.createExecutableExtension("class");
			} catch (CoreException ce) {
				ce.printStackTrace();
				continue;
			}
			validators.add(validator);
			instance.addValidator(validator);
		}
	}
	
	private void uninstallValidators() {
		ValidatorManager instance = ValidatorManager.getInstance();
		for (IStepValidator i : validators)
			instance.removeValidator(i);
		validators.clear();
		
		validators = null;
	}
}
