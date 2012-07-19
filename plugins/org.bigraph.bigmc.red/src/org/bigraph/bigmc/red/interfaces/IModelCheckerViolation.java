package org.bigraph.bigmc.red.interfaces;

import org.bigraph.bigmc.red.interfaces.IModelChecker.IProperty;

/**
 * An <strong>IModelCheckerViolation</strong> represents a property violation
 * found by an {@link IModelChecker}.
 * @author alec
 */
public interface IModelCheckerViolation {
	/**
	 * Returns the {@link IProperty} that was violated.
	 * @return an {@link IProperty}
	 */
	IProperty getProperty();
	
	/**
	 * Returns the trace through the state space which led to this property
	 * violation.
	 * @return an array of {@link IModelCheckerTransition} steps
	 */
	IModelCheckerTransition[] getTrace();
}
