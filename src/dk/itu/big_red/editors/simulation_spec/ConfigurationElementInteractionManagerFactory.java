package dk.itu.big_red.editors.simulation_spec;

import org.eclipse.core.runtime.IConfigurationElement;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.interaction_managers.IInteractionManager;
import dk.itu.big_red.interaction_managers.IInteractionManagerFactory;

/**
 * The <strong>ConfigurationElementInteractionManagerFactory</strong> creates
 * {@link IInteractionManager}s from {@link IConfigurationElement}s.
 * @author alec
 * @see RedPlugin#instantiate(IConfigurationElement)
 */
class ConfigurationElementInteractionManagerFactory
	implements IInteractionManagerFactory {
	private IConfigurationElement ice = null;
	
	public IConfigurationElement getCE() {
		return ice;
	}
	
	public ConfigurationElementInteractionManagerFactory(IConfigurationElement ice) {
		this.ice = ice;
	}
	
	@Override
	public String getName() {
		return getCE().getAttribute("name");
	}
	
	@Override
	public IInteractionManager createInteractionManager() {
		return (IInteractionManager)RedPlugin.instantiate(getCE());
	}
}