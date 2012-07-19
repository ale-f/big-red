package dk.itu.big_red.interaction_managers;

import org.bigraph.model.SimulationSpec;

/**
 * A basic concrete implementation of {@link IInteractionManager}.
 * @author alec
 */
public abstract class InteractionManager implements IInteractionManager {
	private SimulationSpec simulationSpec = null;
	
	@Override
	public IInteractionManager setSimulationSpec(SimulationSpec s) {
		simulationSpec = s;
		return this;
	}
	
	protected SimulationSpec getSimulationSpec() {
		return simulationSpec;
	}
}
