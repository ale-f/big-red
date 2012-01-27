package dk.itu.big_red.tools;

import org.eclipse.core.runtime.IConfigurationElement;

import dk.itu.big_red.application.plugin.RedPlugin;

public class InteractionManagerFactory {
	public static IInteractionManager createInteractionManager(IConfigurationElement ice) {
		Object o = RedPlugin.instantiate(ice);
		if (o instanceof IInteractionManager) {
			return (IInteractionManager)o;
		} else return null;
	}
}
