package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IInnerName;

/**
 * 
 * @author alec
 * @see IInnerName
 */
public class InnerName extends Point implements IInnerName {
	public static final class Identifier extends Point.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public InnerName lookup(Bigraph universe, PropertyScratchpad context) {
			return (InnerName)
				universe.getNamespace(InnerName.class).get(context, getName());
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
