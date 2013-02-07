package org.bigraph.model.assistants;

import java.util.ArrayDeque;

import org.bigraph.model.ModelObject.Identifier;
import org.bigraph.model.ModelObject.Identifier.Resolver;

public class ResolverDeque extends ArrayDeque<Resolver> implements Resolver {
	private static final long serialVersionUID = 4350125917063763714L;
	
	@Override
	public Object lookup(PropertyScratchpad context, Identifier identifier) {
		for (Resolver i : this) {
			Object result = i.lookup(context, identifier);
			if (result != null)
				return result;
		}
		return null;
	}
}
