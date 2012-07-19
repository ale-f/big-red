package org.bigraph.bigmc.red.interfaces;

/**
 * An <strong>IModelChecker</strong> is an abstract interface to a bigraphical
 * model checking tool.
 * @author alec
 */
public interface IModelChecker {
	interface IBigraph { /* placeholder */ }
	interface IProperty { /* placeholder */ }
	interface ISignature { /* placeholder */ }
	interface IReactionRule { /* placeholder */ }
	
	/**
	 * Resets the state of this model checker.
	 */
	void reset();
	
	void setModel(IBigraph bigraph);
	void addProperty(IProperty property);
	void setSignature(ISignature signature);
	void addReactionRule(IReactionRule rule);
	
	IModelCheckerResult run(IModelCheckerMonitor monitor);
}
