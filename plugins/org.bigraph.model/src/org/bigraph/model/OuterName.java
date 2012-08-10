package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IOuterName;

/**
 * @author alec
 * @see IOuterName
 */
public class OuterName extends Link implements IOuterName {
	public static final class Identifier extends Link.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public OuterName lookup(PropertyScratchpad context, Resolver r) {
			return require(super.lookup(context, r), OuterName.class);
		}
		
		@Override
		public Identifier getRenamed(String name) {
			return new Identifier(name);
		}
		
		@Override
		public String toString() {
			return "outer name " + getName();
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
