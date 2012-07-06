package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IOuterName;

/**
 * 
 * @author alec
 * @see IOuterName
 */
public class OuterName extends Link implements IOuterName {
	public static final class Identifier extends Link.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public OuterName lookup(Bigraph universe, PropertyScratchpad context) {
			return (OuterName)super.lookup(universe, context);
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
