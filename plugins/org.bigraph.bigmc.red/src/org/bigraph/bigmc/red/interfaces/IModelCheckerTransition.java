package org.bigraph.bigmc.red.interfaces;

import org.bigraph.bigmc.red.interfaces.IModelChecker.IReactionRule;

/**
 * An <strong>IModelCheckerTransition</strong> represents a single step through
 * an {@link IModelCheckerResult}'s state space.
 * @author alec
 */
public interface IModelCheckerTransition {
	/**
	 * Returns the reaction rule which transformed the pre-state into the
	 * post-state.
	 * @return an {@link IReactionRule}
	 * @see #getPreState()
	 * @see #getPostState()
	 */
	IReactionRule getRule();
	
	/**
	 * Returns the state of the model before this transition.
	 * @return an {@link IModelCheckerState}
	 * @see #getPostState()
	 */
	IModelCheckerState getPreState();
	
	/**
	 * Returns the state of the model after this transition.
	 * @return an {@link IModelCheckerState}
	 * @see #getPreState()
	 */
	IModelCheckerState getPostState();
}
