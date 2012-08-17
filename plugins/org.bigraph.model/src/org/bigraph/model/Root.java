package org.bigraph.model;

import java.util.List;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IChild;
import org.bigraph.model.interfaces.IRoot;

/**
 * @author alec
 * @see IRoot
 */
public class Root extends Container implements IRoot {
	@Override
	public boolean canContain(Layoutable child) {
		Class<? extends Layoutable> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}

	@Override
	public List<? extends Node> getNodes() {
		return only(null, Node.class);
	}

	@Override
	public List<? extends Site> getSites() {
		return only(null, Site.class);
	}

	@Override
	public List<? extends IChild> getIChildren() {
		return only(null, IChild.class);
	}
	
	public static final class Identifier extends Container.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public Root lookup(PropertyScratchpad context, Resolver r) {
			return require(r.lookup(context, this), Root.class);
		}
		
		@Override
		public Identifier getRenamed(String name) {
			return new Identifier(name);
		}
		
		@Override
		public String toString() {
			return "root " + getName();
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
