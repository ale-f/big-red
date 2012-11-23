package org.bigraph.model.changes.descriptors;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.Identifier;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.IChange;

/**
 * Classes implementing <strong>IChangeDescriptor</strong> are <i>change
 * descriptors</i>: objects that know how to construct an {@link IChange}.
 * They're essentially just changes which target {@link Identifier}s rather
 * than {@link ModelObject}s.
 * @author alec
 * @see IChange
 */
public interface IChangeDescriptor {
	/**
	 * Instantiates an {@link IChange} from this descriptor.
	 * @param context a {@link PropertyScratchpad}
	 * @param r a {@link Resolver} for resolving {@link Identifier}s
	 * @return a new {@link IChange} (not <code>null</code>)
	 * @throws ChangeCreationException if something went wrong
	 */
	IChange createChange(PropertyScratchpad context, Resolver r)
			throws ChangeCreationException;
	
	IChangeDescriptor inverse();
}