package dk.itu.big_red.tools;

import dk.itu.big_red.model.SimulationSpec;

/**
 * Classes implementing <strong>IInteractionManager</strong> are <i>interaction
 * managers</i>, intentionally minimally-specified objects which provide a
 * flexible interface between Big Red and external tools.
 * @author alec
 *
 */
public interface IInteractionManager {
	public static final String EXTENSION_POINT = "dk.itu.big_red.interactionManagers";
	
	/**
	 * Sets the {@link SimulationSpec} for this {@link IInteractionManager}.
	 * @param s a {@link SimulationSpec}
	 * @return <code>this</code>, for convenience
	 */
	public IInteractionManager setSimulationSpec(SimulationSpec s);
	
	/**
	 * Starts this {@link IInteractionManager}. This method will block until
	 * the {@link IInteractionManager} has finished.
	 * <p>(<strong>Important note:</strong> this method is allowed to do
	 * <i>anything</i>, including &mdash; but by no means limited to &mdash;
	 * modifying the Eclipse workspace and displaying modal dialogs.)
	 */
	public void run();
}
