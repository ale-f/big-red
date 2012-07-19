package org.bigraph.bigmc.red.interfaces;

/**
 * An <strong>IModelCheckerResult</strong> is the result of running an {@link
 * IModelChecker}. It contains the reaction graph, and, if there was one, the
 * property violation which caused the execution to stop.
 * @author alec
 */
public interface IModelCheckerResult {
	/**
	 * An <strong>Outcome</strong> indicates the kind of result represented by
	 * an {@link IModelCheckerResult}.
	 * @author alec
	 */
	enum Outcome {
		/**
		 * The state space was exhausted, and the reaction graph is complete.
		 */
		COMPLETE,
		/**
		 * The maximum number of states was exceeded, so the reaction graph is
		 * not complete.
		 */
		INCOMPLETE,
		/**
		 * A property was violated, so the reaction graph is not complete.
		 */
		PROPERTY_VIOLATION
	}
	
	/**
	 * Returns the outcome of running the {@link IModelChecker}.
	 * @return an {@link Outcome}
	 * @see Outcome#COMPLETE
	 * @see Outcome#INCOMPLETE
	 * @see Outcome#PROPERTY_VIOLATION
	 */
	Outcome getOutcome();
	
	/**
	 * Returns the start state.
	 * @return an {@link IModelCheckerState}
	 * @see #getOutcome()
	 */
	IModelCheckerState getStartState();
	
	/**
	 * Returns the property violation that caused the {@link IModelChecker}'s
	 * execution to stop.
	 * @return an {@link IModelCheckerViolation}, if a violation was found, or
	 * <code>null</code> otherwise
	 * @see #getOutcome()
	 */
	IModelCheckerViolation getViolation();
	
	/**
	 * Disposes of this set of results.
	 */
	void dispose();
}
