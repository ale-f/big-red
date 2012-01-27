package dk.itu.big_red.tools;

import dk.itu.big_red.model.SimulationSpec;

/**
 * Classes implementing <strong>IInteractionManager</strong> are <i>interaction
 * managers</i>; given a {@link SimulationSpec}, they build a 
 * @author alec
 *
 */
public interface IInteractionManager {
	public IInteractionManager setSimulationSpec(SimulationSpec s);
	
	public void run();
}
