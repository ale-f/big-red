package dk.itu.big_red.interaction_managers;

import org.bigraph.model.SimulationSpec;
import org.eclipse.swt.widgets.Shell;

/**
 * Classes implementing <strong>IInteractionManager</strong> are <i>interaction
 * managers</i>, intentionally minimally-specified objects which provide a
 * flexible interface between Big Red and external tools.
 * @author alec
 */
public interface IInteractionManager {
	String EXTENSION_POINT = "dk.itu.big_red.interactionManagers";
	
	/**
	 * Sets the {@link SimulationSpec} for this {@link IInteractionManager}.
	 * @param s a {@link SimulationSpec}
	 * @return <code>this</code>, for convenience
	 */
	IInteractionManager setSimulationSpec(SimulationSpec s);
	
	/**
	 * Starts this {@link IInteractionManager}. This method will block until
	 * the {@link IInteractionManager} has finished.
	 * <p>(<strong>Important note:</strong> this method is allowed to do
	 * <i>anything</i>, including &mdash; but by no means limited to &mdash;
	 * modifying the Eclipse workspace and displaying modal dialogs.)
	 * @param parent the caller's {@link Shell}, if one exists
	 */
	void run(Shell parent);
}
