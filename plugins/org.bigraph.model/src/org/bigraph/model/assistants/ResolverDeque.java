package org.bigraph.model.assistants;

import java.util.ArrayDeque;


public class ResolverDeque extends ArrayDeque<IObjectIdentifier.Resolver>
		implements IObjectIdentifier.Resolver {
	private static final long serialVersionUID = 4350125917063763714L;
	
	@Override
	public Object lookup(
			PropertyScratchpad context, IObjectIdentifier identifier) {
		for (IObjectIdentifier.Resolver i : this) {
			Object result = i.lookup(context, identifier);
			if (result != null)
				return result;
		}
		return null;
	}
}
