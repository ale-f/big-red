package org.bigraph.model.assistants;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.Identifier;

/**
 * Classes implementing <strong>Identifier</strong> are <i>abstract object
 * identifiers</i> &mdash; that is, they refer to {@link ModelObject}s in
 * a less specific way than a Java object reference does.
 * @author alec
 */
public interface IObjectIdentifier {
	/**
	 * Classes implementing <strong>Resolver</strong> can resolve {@link
	 * Identifier}s into {@link Object}s.
	 * @author alec
	 */
	interface Resolver {
		Object lookup(
				PropertyScratchpad context, IObjectIdentifier identifier);
	}
	
	/**
	 * Retrieves the {@link ModelObject} corresponding to this {@link
	 * Identifier} from the given {@link Resolver}.
	 * @param context a {@link PropertyScratchpad} containing changes to
	 * the {@link Resolver}'s state; can be <code>null</code>
	 * @param r a {@link Resolver}
	 * @return a {@link ModelObject}, or <code>null</code> if the lookup
	 * failed
	 */
	Object lookup(PropertyScratchpad context, Resolver r);
}