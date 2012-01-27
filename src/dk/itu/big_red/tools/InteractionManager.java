package dk.itu.big_red.tools;

import dk.itu.big_red.model.SimulationSpec;

/**
 * A basic concrete implementation of {@link IInteractionManager}.
 * @author alec
 *
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
