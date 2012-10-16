package org.bigraph.model.wrapper;

import java.util.ArrayList;

import org.bigraph.model.assistants.ValidatorManager;
import org.bigraph.model.changes.IChangeValidator2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.RegistryFactory;
import org.osgi.framework.BundleContext;

public final class Activator extends Plugin {
	private static Activator instance;
	
	private ArrayList<IChangeValidator2> validators;
	
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
		validators = new ArrayList<IChangeValidator2>();
		
		ValidatorManager instance = ValidatorManager.getInstance();
		IExtensionRegistry registry = RegistryFactory.getRegistry();
		for (IConfigurationElement ice :
				registry.getConfigurationElementsFor(
						EXTENSION_POINT_VALIDATION)) {
			IChangeValidator2 validator;
			try {
				validator = (IChangeValidator2)
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
		for (IChangeValidator2 i : validators)
			instance.removeValidator(i);
		validators.clear();
		
		validators = null;
	}
}
