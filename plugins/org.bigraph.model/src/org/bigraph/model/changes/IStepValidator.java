package org.bigraph.model.changes;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator.Process;
import org.bigraph.model.process.IParticipant;

/**
 * Classes implementing <strong>IStepValidator</strong> can validate {@link
 * IChange}s as part of a {@link Process}.
 * @author alec
 */
public interface IStepValidator extends IParticipant {
	/**
	 * Validates an {@link IChange}.
	 * @param context the {@link Process} in which validation is taking place
	 * @param change the {@link IChange} to be validated, which is
	 * guaranteed:&mdash;
	 * <ul>
	 * <li>not to be {@code null};
	 * <li>to be {@link IChange#isReady() ready};
	 * <li>not to be an {@link IChange.Group}; and
	 * <li>not to have been {@link IChange#simulate(PropertyScratchpad, Resolver)
	 * simulated} in {@code context}'s {@link Process#getScratch() scratchpad}.
	 * </ul>
	 * @return <code>true</code> if the change was recognised and validated, or
	 * <code>false</code> otherwise
	 * @throws ChangeCreationException if the change was recognised and failed
	 * validation
	 */
	boolean tryValidateChange(Process context, IChange change)
			throws ChangeCreationException;
}
