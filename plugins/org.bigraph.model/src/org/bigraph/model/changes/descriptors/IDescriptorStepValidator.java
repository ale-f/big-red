package org.bigraph.model.changes.descriptors;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.process.IParticipant;

/**
 * Classes implementing <strong>IDescriptorStepValidator</strong> can validate
 * {@link IChangeDescriptor}s as part of a {@link Process}.
 * @author alec
 */
public interface IDescriptorStepValidator extends IParticipant {
	/**
	 * Classes implementing <strong>Callback</strong> are called by a {@link
	 * Process} just before it finishes, and so can be used to implement a
	 * second validation phase.
	 * @author alec
	 */
	interface Callback {
		/**
		 * Executes this {@link Callback}.
		 * @throws ChangeCreationException if final validation failed
		 */
		void run() throws ChangeCreationException;
	}
	
	/**
	 * Classes implementing <strong>Process</strong> are validation processes
	 * which use {@link IDescriptorStepValidator}s to perform validation.
	 * @author alec
	 */
	interface Process {
		/**
		 * Returns the {@link PropertyScratchpad} used to track the
		 * modifications made by this {@link Process}.
		 * @return a {@link PropertyScratchpad}
		 */
		PropertyScratchpad getScratch();
		
		Resolver getResolver();
		
		/**
		 * Adds a {@link Callback} to this {@link Process}.
		 * @param c a {@link Callback}
		 */
		void addCallback(Callback c);
	}
	
	/**
	 * Validates an {@link IChangeDescriptor}.
	 * @param context the {@link Process} in which validation is taking place
	 * @param change the {@link IChangeDescriptor} to be validated, which is
	 * guaranteed:&mdash;
	 * <ul>
	 * <li>not to be {@code null};
	 * <li>not to be an {@link IChangeDescriptor.Group}; and
	 * <li>not to have been {@link IChangeDescriptor#simulate(Resolver,
	 * PropertyScratchpad) simulated} in {@code context}'s {@link
	 * Process#getScratch() scratchpad}.
	 * </ul>
	 * @return <code>true</code> if the change was recognised and validated, or
	 * <code>false</code> otherwise
	 * @throws ChangeCreationException if the change was recognised and failed
	 * validation
	 */
	boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException;
}
