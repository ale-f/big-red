package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IParent;
import org.bigraph.model.interfaces.ISite;

/**
 * @author alec
 * @see ISite
 */
public class Site extends Layoutable implements ISite {
	@Override
	public IParent getIParent() {
		return (IParent)getParent();
	}
	
	public static final class Identifier extends Layoutable.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public Site lookup(Bigraph universe, PropertyScratchpad context) {
			return (Site)
					universe.getNamespace(Site.class).get(context, getName());
		}
		
		@Override
		public String toString() {
			return "site " + getName();
		}
	}
	
	@Override
	public Identifier getIdentifier() {
		return getIdentifier(null);
	}
	
	@Override
	public Identifier getIdentifier(PropertyScratchpad context) {
		return new Identifier(getName(context));
	}
}
