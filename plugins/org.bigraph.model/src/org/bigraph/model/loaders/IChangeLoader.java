package org.bigraph.model.loaders;

import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.IChange;

public interface IChangeLoader extends ILoader {
	/**
	 * Adds and simulates an {@link IChange}.
	 * @param c an {@link IChange}
	 * @see #getChanges()
	 * @see #getScratch()
	 */
	void addChange(IChange c);
	
	Resolver getResolver();
	
	/**
	 * Returns a {@link ChangeGroup} containing all of the added changes.
	 * @return a {@link ChangeGroup}
	 */
	ChangeGroup getChanges();
	
	/**
	 * Returns the {@link PropertyScratchpad} in which the added changes have
	 * been simulated.
	 * @return a {@link PropertyScratchpad}
	 */
	PropertyScratchpad getScratch();
}
