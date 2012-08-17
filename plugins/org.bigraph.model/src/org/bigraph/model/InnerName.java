package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IInnerName;

/**
 * @author alec
 * @see IInnerName
 */
public class InnerName extends Point implements IInnerName {
	public static final class Identifier extends Point.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public InnerName lookup(PropertyScratchpad context, Resolver r) {
			return require(r.lookup(context, this), InnerName.class);
		}
		
		@Override
		public Identifier getRenamed(String name) {
			return new Identifier(name);
		}
		
		@Override
		public String toString() {
			return "inner name " + getName();
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
