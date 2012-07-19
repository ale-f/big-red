package org.bigraph.bigmc.red.interfaces;

import org.bigraph.bigmc.red.interfaces.IModelChecker.IBigraph;

/**
 * An <strong>IModelCheckerState</strong> represents a state in the state space
 * of an {@link IModelCheckerResult}.
 * @author alec
 */
public interface IModelCheckerState {
	/**
	 * Returns the state of the model.
	 * @return an {@link IBigraph}
	 */
	IBigraph getModel();
	
	/**
	 * Returns the transitions for which this state is a post-state.
	 * @return an array of {@link IModelCheckerTransition}s
	 */
	IModelCheckerTransition[] getPrecursors();
	
	/**
	 * Returns the transitions for which this state is a pre-state.
	 * @return an array of {@link IModelCheckerTransition}s
	 */
	IModelCheckerTransition[] getSuccessors();
}
