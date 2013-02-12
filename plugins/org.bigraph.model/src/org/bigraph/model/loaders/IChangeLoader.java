package org.bigraph.model.loaders;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public interface IChangeLoader extends ILoader {
	/**
	 * Adds and simulates an {@link IChangeDescriptor}.
	 * @param c an {@link IChangeDescriptor}
	 * @see #getChanges()
	 * @see #getScratch()
	 */
	void addChange(IChangeDescriptor c);
	
	Resolver getResolver();
	
	/**
	 * Returns a {@link ChangeDescriptorGroup} containing all of the added
	 * changes.
	 * @return a {@link ChangeDescriptorGroup}
	 */
	ChangeDescriptorGroup getChanges();
	
	/**
	 * Returns the {@link PropertyScratchpad} in which the added changes have
	 * been simulated.
	 * @return a {@link PropertyScratchpad}
	 */
	PropertyScratchpad getScratch();
}
